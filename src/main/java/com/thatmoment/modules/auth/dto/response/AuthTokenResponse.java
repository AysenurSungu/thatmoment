package com.thatmoment.modules.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class AuthTokenResponse {

    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private String tokenType;
    private UUID userId;
    private String email;
    private UUID sessionId;

    public static AuthTokenResponse of(
            String accessToken,
            String refreshToken,
            long expiresIn,
            UUID userId,
            String email,
            UUID sessionId
    ) {
        return AuthTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .tokenType("Bearer")
                .userId(userId)
                .email(email)
                .sessionId(sessionId)
                .build();
    }
}
