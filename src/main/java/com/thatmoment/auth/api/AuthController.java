package com.thatmoment.auth.api;

import com.thatmoment.auth.dto.request.LoginRequest;
import com.thatmoment.auth.dto.request.LoginVerifyRequest;
import com.thatmoment.auth.dto.request.LogoutRequest;
import com.thatmoment.auth.dto.request.RefreshTokenRequest;
import com.thatmoment.auth.dto.request.RegisterRequest;
import com.thatmoment.auth.dto.request.ResendCodeRequest;
import com.thatmoment.auth.dto.request.VerifyEmailRequest;
import com.thatmoment.auth.dto.response.AuthTokenResponse;
import com.thatmoment.auth.dto.response.RegisterResponse;
import com.thatmoment.auth.dto.response.SessionResponse;
import com.thatmoment.auth.domain.Session;
import com.thatmoment.auth.security.UserPrincipal;
import com.thatmoment.auth.service.AuthService;
import com.thatmoment.common.constants.ApiDescriptions;
import com.thatmoment.common.constants.AuthMessages;
import com.thatmoment.common.dto.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.net.InetAddress;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = ApiDescriptions.TAG_AUTH, description = ApiDescriptions.TAG_AUTH_DESC)
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
            summary = ApiDescriptions.REGISTER_SUMMARY,
            description = ApiDescriptions.REGISTER_DESCRIPTION
    )
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/verify-email")
    @Operation(
            summary = ApiDescriptions.VERIFY_EMAIL_SUMMARY,
            description = ApiDescriptions.VERIFY_EMAIL_DESCRIPTION
    )
    public ResponseEntity<MessageResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        MessageResponse response = authService.verifyEmail(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-code")
    @Operation(
            summary = ApiDescriptions.RESEND_CODE_SUMMARY,
            description = ApiDescriptions.RESEND_CODE_DESCRIPTION
    )
    public ResponseEntity<MessageResponse> resendCode(@Valid @RequestBody ResendCodeRequest request) {
        MessageResponse response = authService.resendVerificationCode(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(
            summary = ApiDescriptions.LOGIN_SUMMARY,
            description = ApiDescriptions.LOGIN_DESCRIPTION
    )
    public ResponseEntity<MessageResponse> login(@Valid @RequestBody LoginRequest request) {
        MessageResponse response = authService.sendLoginCode(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login/verify")
    @Operation(
            summary = ApiDescriptions.LOGIN_VERIFY_SUMMARY,
            description = ApiDescriptions.LOGIN_VERIFY_DESCRIPTION
    )
    public ResponseEntity<AuthTokenResponse> verifyLogin(
            @Valid @RequestBody LoginVerifyRequest request,
            HttpServletRequest httpRequest
    ) {
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthTokenResponse response = authService.verifyLoginCode(request, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(
            summary = ApiDescriptions.REFRESH_SUMMARY,
            description = ApiDescriptions.REFRESH_DESCRIPTION
    )
    public ResponseEntity<AuthTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthTokenResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(
            summary = ApiDescriptions.LOGOUT_SUMMARY,
            description = ApiDescriptions.LOGOUT_DESCRIPTION
    )
    public ResponseEntity<MessageResponse> logout(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody(required = false) LogoutRequest request
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(MessageResponse.of(AuthMessages.AUTHENTICATION_REQUIRED));
        }

        boolean allDevices = request != null && request.isAllDevices();

        authService.logout(principal.getUserId(), principal.getSessionId(), allDevices);
        return ResponseEntity.ok(MessageResponse.of(AuthMessages.LOGOUT_SUCCESS));
    }

    @GetMapping("/sessions")
    @Operation(
            summary = ApiDescriptions.SESSIONS_SUMMARY,
            description = ApiDescriptions.SESSIONS_DESCRIPTION
    )
    public ResponseEntity<List<SessionResponse>> getActiveSessions(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Session> sessions = authService.getActiveSessions(principal.getUserId());
        List<SessionResponse> response = sessions.stream()
                .map(s -> SessionResponse.builder()
                        .id(s.getId())
                        .deviceName(s.getDeviceName())
                        .platform(s.getPlatform())
                        .ipAddress(formatIp(s.getIpAddress()))
                        .lastActivityAt(s.getLastActivityAt())
                        .createdAt(s.getCreatedAt())
                        .isCurrent(s.getId().equals(principal.getSessionId()))
                        .build())
                .toList();

        return ResponseEntity.ok(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String formatIp(InetAddress ipAddress) {
        return ipAddress != null ? ipAddress.getHostAddress() : null;
    }
}
