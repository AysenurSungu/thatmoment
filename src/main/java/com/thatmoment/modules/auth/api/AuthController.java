package com.thatmoment.modules.auth.api;

import com.thatmoment.modules.auth.dto.request.LoginRequest;
import com.thatmoment.modules.auth.dto.request.LoginVerifyRequest;
import com.thatmoment.modules.auth.dto.request.LogoutRequest;
import com.thatmoment.modules.auth.dto.request.RefreshTokenRequest;
import com.thatmoment.modules.auth.dto.request.RegisterRequest;
import com.thatmoment.modules.auth.dto.request.ResendCodeRequest;
import com.thatmoment.modules.auth.dto.request.VerifyEmailRequest;
import com.thatmoment.modules.auth.dto.response.AuthTokenResponse;
import com.thatmoment.modules.auth.dto.response.RegisterResponse;
import com.thatmoment.modules.auth.dto.response.SessionResponse;
import com.thatmoment.modules.auth.domain.Session;
import com.thatmoment.modules.auth.security.UserPrincipal;
import com.thatmoment.modules.auth.service.AuthService;
import com.thatmoment.common.constants.ApiDescriptions;
import com.thatmoment.common.constants.AuthMessages;
import com.thatmoment.common.dto.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/verify-email")
    @Operation(
            summary = ApiDescriptions.VERIFY_EMAIL_SUMMARY,
            description = ApiDescriptions.VERIFY_EMAIL_DESCRIPTION
    )
    public MessageResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        return authService.verifyEmail(request);
    }

    @PostMapping("/resend-code")
    @Operation(
            summary = ApiDescriptions.RESEND_CODE_SUMMARY,
            description = ApiDescriptions.RESEND_CODE_DESCRIPTION
    )
    public MessageResponse resendCode(@Valid @RequestBody ResendCodeRequest request) {
        return authService.resendVerificationCode(request);
    }

    @PostMapping("/login")
    @Operation(
            summary = ApiDescriptions.LOGIN_SUMMARY,
            description = ApiDescriptions.LOGIN_DESCRIPTION
    )
    public MessageResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.sendLoginCode(request);
    }

    @PostMapping("/login/verify")
    @Operation(
            summary = ApiDescriptions.LOGIN_VERIFY_SUMMARY,
            description = ApiDescriptions.LOGIN_VERIFY_DESCRIPTION
    )
    public AuthTokenResponse verifyLogin(
            @Valid @RequestBody LoginVerifyRequest request,
            HttpServletRequest httpRequest
    ) {
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        return authService.verifyLoginCode(request, ipAddress, userAgent);
    }

    @PostMapping("/refresh")
    @Operation(
            summary = ApiDescriptions.REFRESH_SUMMARY,
            description = ApiDescriptions.REFRESH_DESCRIPTION
    )
    public AuthTokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request);
    }

    @PostMapping("/logout")
    @Operation(
            summary = ApiDescriptions.LOGOUT_SUMMARY,
            description = ApiDescriptions.LOGOUT_DESCRIPTION
    )
    @PreAuthorize("isAuthenticated()")
    public MessageResponse logout(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody(required = false) LogoutRequest request
    ) {
        boolean allDevices = request != null && request.isAllDevices();

        authService.logout(principal.getUserId(), principal.getSessionId(), allDevices);
        return MessageResponse.of(AuthMessages.LOGOUT_SUCCESS);
    }

    @GetMapping("/sessions")
    @Operation(
            summary = ApiDescriptions.SESSIONS_SUMMARY,
            description = ApiDescriptions.SESSIONS_DESCRIPTION
    )
    @PreAuthorize("isAuthenticated()")
    public List<SessionResponse> getActiveSessions(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
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

        return response;
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
