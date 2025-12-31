package com.thatmoment.modules.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class RegisterResponse {

    private UUID userId;
    private String message;
}
