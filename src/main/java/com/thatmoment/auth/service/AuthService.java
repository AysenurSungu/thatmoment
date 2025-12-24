package com.thatmoment.auth.service;

import com.thatmoment.auth.domain.EmailVerification;
import com.thatmoment.auth.domain.RefreshToken;
import com.thatmoment.auth.domain.Session;
import com.thatmoment.auth.domain.User;
import com.thatmoment.auth.domain.enums.AuthMethod;
import com.thatmoment.auth.domain.enums.VerificationPurpose;
import com.thatmoment.auth.dto.request.LoginRequest;
import com.thatmoment.auth.dto.request.LoginVerifyRequest;
import com.thatmoment.auth.dto.request.RefreshTokenRequest;
import com.thatmoment.auth.dto.request.RegisterRequest;
import com.thatmoment.auth.dto.request.ResendCodeRequest;
import com.thatmoment.auth.dto.request.VerifyEmailRequest;
import com.thatmoment.auth.dto.response.AuthTokenResponse;
import com.thatmoment.auth.dto.response.RegisterResponse;
import com.thatmoment.auth.repository.EmailVerificationRepository;
import com.thatmoment.auth.repository.UserRepository;
import com.thatmoment.common.constants.AuthMessages;
import com.thatmoment.common.dto.MessageResponse;
import com.thatmoment.common.exception.exceptions.ConflictException;
import com.thatmoment.common.exception.exceptions.BadRequestException;
import com.thatmoment.common.exception.exceptions.ForbiddenException;
import com.thatmoment.common.exception.exceptions.NotFoundException;
import com.thatmoment.common.exception.exceptions.UnauthorizedException;
import com.thatmoment.common.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final int CODE_EXPIRY_MINUTES = 15;
    private static final int MAX_ATTEMPTS = 3;
    private static final int LOGIN_CODE_EXPIRY_MINUTES = 5;

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final SessionService sessionService;
    private final SecureRandom secureRandom = new SecureRandom();

    public RegisterResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmailAndDeletedAtIsNull(email)) {
            throw new ConflictException(AuthMessages.EMAIL_ALREADY_REGISTERED);
        }

        User user = User.builder()
                .email(email)
                .authMethod(AuthMethod.EMAIL)
                .build();
        User savedUser = userRepository.save(user);

        String code = generateVerificationCode();

        EmailVerification verification = EmailVerification.builder()
                .userId(savedUser.getId())
                .code(code)
                .purpose(VerificationPurpose.EMAIL_VERIFY)
                .maxAttempts(MAX_ATTEMPTS)
                .expiresAt(Instant.now().plusSeconds(CODE_EXPIRY_MINUTES * 60L))
                .build();
        emailVerificationRepository.save(verification);

        emailService.sendVerificationCode(email, code, "EMAIL_VERIFY");

        return RegisterResponse.builder()
                .userId(savedUser.getId())
                .message(AuthMessages.REGISTRATION_SUCCESS)
                .build();
    }

    @Transactional
    public MessageResponse verifyEmail(VerifyEmailRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new NotFoundException(AuthMessages.USER_NOT_FOUND));

        if (Boolean.TRUE.equals(user.getIsVerified())) {
            throw new BadRequestException(AuthMessages.EMAIL_ALREADY_VERIFIED);
        }

        EmailVerification verification = findActiveVerification(
                user.getId(),
                VerificationPurpose.EMAIL_VERIFY,
                AuthMessages.NO_ACTIVE_VERIFICATION_CODE
        );

        if (!verification.matches(request.getCode())) {
            verification.incrementAttempt();
            emailVerificationRepository.save(verification);

            int remaining = verification.getMaxAttempts() - verification.getAttemptCount();
            if (remaining <= 0) {
                throw new BadRequestException(AuthMessages.TOO_MANY_FAILED_ATTEMPTS);
            }
            throw new BadRequestException(String.format(AuthMessages.INVALID_CODE_REMAINING_FORMAT, remaining));
        }

        verification.markAsVerified();
        emailVerificationRepository.save(verification);

        user.markAsVerified();
        userRepository.save(user);

        log.info("Email verified for user: {}", user.getId());
        return MessageResponse.of(AuthMessages.EMAIL_VERIFIED_SUCCESS);
    }

    @Transactional
    public MessageResponse resendVerificationCode(ResendCodeRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new NotFoundException(AuthMessages.USER_NOT_FOUND));

        if (Boolean.TRUE.equals(user.getIsVerified())) {
            throw new BadRequestException(AuthMessages.EMAIL_ALREADY_VERIFIED);
        }

        emailVerificationRepository.invalidatePendingVerifications(
                user.getId(), VerificationPurpose.EMAIL_VERIFY, Instant.now()
        );

        String code = generateVerificationCode();
        EmailVerification verification = EmailVerification.builder()
                .userId(user.getId())
                .code(code)
                .purpose(VerificationPurpose.EMAIL_VERIFY)
                .maxAttempts(MAX_ATTEMPTS)
                .expiresAt(Instant.now().plus(CODE_EXPIRY_MINUTES, ChronoUnit.MINUTES))
                .build();

        emailVerificationRepository.save(verification);

        emailService.sendVerificationCode(email, code, "EMAIL_VERIFY");

        log.info("Verification code resent to: {}", email);
        return MessageResponse.of(AuthMessages.VERIFICATION_CODE_SENT);
    }

    @Transactional
    public MessageResponse sendLoginCode(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new NotFoundException(AuthMessages.USER_NOT_FOUND));

        if (!Boolean.TRUE.equals(user.getIsVerified())) {
            throw new BadRequestException(AuthMessages.EMAIL_NOT_VERIFIED);
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new ForbiddenException(AuthMessages.ACCOUNT_SUSPENDED);
        }

        if (user.isLocked()) {
            throw new ForbiddenException(AuthMessages.ACCOUNT_LOCKED);
        }

        emailVerificationRepository.invalidatePendingVerifications(
                user.getId(), VerificationPurpose.LOGIN_OTP, Instant.now()
        );

        String code = generateVerificationCode();
        EmailVerification verification = EmailVerification.builder()
                .userId(user.getId())
                .code(code)
                .purpose(VerificationPurpose.LOGIN_OTP)
                .maxAttempts(MAX_ATTEMPTS)
                .expiresAt(Instant.now().plus(LOGIN_CODE_EXPIRY_MINUTES, ChronoUnit.MINUTES))
                .build();

        emailVerificationRepository.save(verification);

        emailService.sendVerificationCode(email, code, "LOGIN_OTP");

        log.info("Login code sent to: {}", email);
        return MessageResponse.of(AuthMessages.LOGIN_CODE_SENT);
    }

    @Transactional
    public AuthTokenResponse verifyLoginCode(LoginVerifyRequest request, String ipAddress, String userAgent) {
        String email = request.getEmail().toLowerCase().trim();

        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new NotFoundException(AuthMessages.USER_NOT_FOUND));

        EmailVerification verification = findActiveVerification(
                user.getId(),
                VerificationPurpose.LOGIN_OTP,
                AuthMessages.NO_ACTIVE_LOGIN_CODE
        );

        if (!verification.matches(request.getCode())) {
            verification.incrementAttempt();
            emailVerificationRepository.save(verification);

            user.recordFailedLogin(5, 30);
            userRepository.save(user);

            int remaining = verification.getMaxAttempts() - verification.getAttemptCount();
            if (remaining <= 0) {
                throw new BadRequestException(AuthMessages.TOO_MANY_FAILED_ATTEMPTS);
            }
            throw new BadRequestException(String.format(AuthMessages.INVALID_CODE_REMAINING_FORMAT, remaining));
        }

        verification.markAsVerified();
        emailVerificationRepository.save(verification);

        user.recordSuccessfulLogin();
        userRepository.save(user);

        String deviceName = parseDeviceName(userAgent);
        String platform = parsePlatform(userAgent);
        Session session = sessionService.createSession(user.getId(), deviceName, platform, ipAddress, userAgent);

        String accessToken = jwtService.generateAccessToken(user.getId(), session.getId(), user.getEmail());
        String refreshToken = sessionService.createRefreshToken(user.getId(), session.getId());

        log.info("User logged in: {} session: {}", user.getId(), session.getId());

        return AuthTokenResponse.of(
                accessToken,
                refreshToken,
                jwtService.getAccessTokenExpirationSeconds(),
                user.getId(),
                user.getEmail(),
                session.getId()
        );
    }

    private String generateVerificationCode() {
        int codeInt = secureRandom.nextInt(900000) + 100000;
        return String.valueOf(codeInt);
    }

    @Transactional
    public AuthTokenResponse refreshToken(RefreshTokenRequest request) {
        String refreshTokenStr = request.getRefreshToken();

        if (!jwtService.isTokenValid(refreshTokenStr)) {
            throw new UnauthorizedException(AuthMessages.INVALID_REFRESH_TOKEN);
        }

        String tokenType = jwtService.extractTokenType(refreshTokenStr);
        if (!"refresh".equals(tokenType)) {
            throw new UnauthorizedException(AuthMessages.INVALID_TOKEN_TYPE);
        }

        RefreshToken tokenEntity = sessionService.validateRefreshToken(refreshTokenStr);

        UUID userId = jwtService.extractUserId(refreshTokenStr);
        UUID sessionId = tokenEntity.getSessionId();
        Session session = sessionService.getValidSession(sessionId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException(AuthMessages.USER_NOT_FOUND));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new UnauthorizedException(AuthMessages.ACCOUNT_SUSPENDED);
        }

        String newAccessToken = jwtService.generateAccessToken(userId, sessionId, user.getEmail());
        String newRefreshToken = sessionService.rotateRefreshToken(tokenEntity, userId);

        sessionService.updateSessionActivity(sessionId);

        log.info("Token refreshed for user: {} session: {}", userId, sessionId);

        return AuthTokenResponse.of(
                newAccessToken,
                newRefreshToken,
                jwtService.getAccessTokenExpirationSeconds(),
                userId,
                user.getEmail(),
                sessionId
        );
    }

    @Transactional
    public void logout(UUID userId, UUID sessionId, boolean allDevices) {
        if (allDevices) {
            sessionService.revokeAllUserSessions(userId, "User logout all devices");
            log.info("User logged out from all devices: {}", userId);
        } else {
            sessionService.revokeSession(sessionId, userId, "User logout");
            log.info("User logged out from session: {}", sessionId);
        }
    }

    @Transactional(readOnly = true)
    public List<Session> getActiveSessions(UUID userId) {
        return sessionService.getActiveSessions(userId);
    }

    private String parseDeviceName(String userAgent) {
        if (userAgent == null) {
            return "Unknown";
        }
        if (userAgent.contains("iPhone")) {
            return "iPhone";
        }
        if (userAgent.contains("iPad")) {
            return "iPad";
        }
        if (userAgent.contains("Android")) {
            return "Android Device";
        }
        if (userAgent.contains("Windows")) {
            return "Windows PC";
        }
        if (userAgent.contains("Mac")) {
            return "Mac";
        }
        if (userAgent.contains("Linux")) {
            return "Linux PC";
        }
        return "Unknown Device";
    }

    private String parsePlatform(String userAgent) {
        if (userAgent == null) {
            return "Unknown";
        }
        if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            return "iOS";
        }
        if (userAgent.contains("Android")) {
            return "Android";
        }
        return "Web";
    }

    private EmailVerification findActiveVerification(UUID userId, VerificationPurpose purpose, String errorMessage) {
        Optional<EmailVerification> verification = emailVerificationRepository
            .findActiveVerification(userId, purpose, Instant.now(), PageRequest.of(0, 1))
            .stream()
            .findFirst();

        return verification.orElseThrow(() -> new BadRequestException(errorMessage));
    }
}
