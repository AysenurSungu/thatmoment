package com.thatmoment.auth.service;

import com.thatmoment.auth.domain.RefreshToken;
import com.thatmoment.auth.domain.Session;
import com.thatmoment.auth.repository.RefreshTokenRepository;
import com.thatmoment.auth.repository.SessionRepository;
import com.thatmoment.common.constants.AuthMessages;
import com.thatmoment.common.exception.exceptions.NotFoundException;
import com.thatmoment.common.exception.exceptions.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

    private final SessionRepository sessionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    public SessionService(
            SessionRepository sessionRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtService jwtService
    ) {
        this.sessionRepository = sessionRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public Session createSession(UUID userId, String deviceName, String platform, String ipAddress, String userAgent) {
        Session session = Session.builder()
                .userId(userId)
                .deviceName(deviceName)
                .platform(platform)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .isActive(true)
                .lastActivityAt(Instant.now())
                .build();

        session = sessionRepository.save(session);
        log.info("Session created: {} for user: {}", session.getId(), userId);
        return session;
    }

    @Transactional
    public String createRefreshToken(UUID userId, UUID sessionId) {
        String refreshToken = jwtService.generateRefreshToken(userId, sessionId);
        String tokenHash = TokenHashUtil.hash(refreshToken);

        RefreshToken tokenEntity = RefreshToken.builder()
                .sessionId(sessionId)
                .tokenHash(tokenHash)
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTokenExpirationSeconds()))
                .isActive(true)
                .build();

        refreshTokenRepository.save(tokenEntity);
        log.debug("Refresh token created for session: {}", sessionId);

        return refreshToken;
    }

    @Transactional
    public RefreshToken validateRefreshToken(String refreshToken) {
        String tokenHash = TokenHashUtil.hash(refreshToken);

        RefreshToken tokenEntity = refreshTokenRepository.findByTokenHashAndIsActiveTrue(tokenHash)
                .orElseThrow(() -> new UnauthorizedException(AuthMessages.INVALID_REFRESH_TOKEN));

        if (tokenEntity.isExpired()) {
            throw new UnauthorizedException(AuthMessages.REFRESH_TOKEN_EXPIRED);
        }

        if (tokenEntity.getUsedAt() != null) {
            log.warn("Refresh token reuse detected for session: {}", tokenEntity.getSessionId());
            revokeAllSessionsBySessionId(tokenEntity.getSessionId(), AuthMessages.TOKEN_REUSE_DETECTED);
            throw new UnauthorizedException(AuthMessages.TOKEN_REUSE_DETECTED);
        }

        return tokenEntity;
    }

    @Transactional
    public String rotateRefreshToken(RefreshToken oldToken, UUID userId) {
        oldToken.markAsUsed();
        refreshTokenRepository.save(oldToken);

        return createRefreshToken(userId, oldToken.getSessionId());
    }

    @Transactional(readOnly = true)
    public Session getValidSession(UUID sessionId, UUID userId) {
        return sessionRepository.findByIdAndUserIdAndIsActiveTrue(sessionId, userId)
                .orElseThrow(() -> new UnauthorizedException(AuthMessages.SESSION_NOT_FOUND_OR_EXPIRED));
    }

    @Transactional
    public void updateSessionActivity(UUID sessionId) {
        sessionRepository.findById(sessionId)
                .ifPresent(session -> {
                    session.updateActivity();
                    sessionRepository.save(session);
                });
    }

    @Transactional
    public void revokeSession(UUID sessionId, UUID userId, String reason) {
        Session session = sessionRepository.findByIdAndUserIdAndIsActiveTrue(sessionId, userId)
                .orElseThrow(() -> new NotFoundException(AuthMessages.SESSION_NOT_FOUND));

        session.revoke(reason);
        sessionRepository.save(session);

        refreshTokenRepository.revokeAllBySessionId(sessionId);

        log.info("Session revoked: {} reason: {}", sessionId, reason);
    }

    @Transactional
    public void revokeAllSessionsBySessionId(UUID sessionId, String reason) {
        sessionRepository.findById(sessionId).ifPresent(session -> {
            UUID userId = session.getUserId();
            revokeAllUserSessions(userId, reason);
        });
    }

    @Transactional
    public void revokeAllUserSessions(UUID userId, String reason) {
        sessionRepository.revokeAllByUserId(userId, reason, Instant.now());
        refreshTokenRepository.revokeAllByUserId(userId);
        log.info("All sessions revoked for user: {} reason: {}", userId, reason);
    }

    @Transactional(readOnly = true)
    public List<Session> getActiveSessions(UUID userId) {
        return sessionRepository.findByUserIdAndIsActiveTrueOrderByLastActivityAtDesc(userId);
    }
}
