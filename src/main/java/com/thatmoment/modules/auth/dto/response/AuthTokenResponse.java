package com.thatmoment.modules.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
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

    public static AuthTokenResponse web(AuthTokenResponse source) {
        return AuthTokenResponse.builder()
                .expiresIn(source.getExpiresIn())
                .tokenType(source.getTokenType())
                .userId(source.getUserId())
                .email(source.getEmail())
                .sessionId(source.getSessionId())
                .build();
    }
}
