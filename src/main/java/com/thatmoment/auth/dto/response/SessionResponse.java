package com.thatmoment.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class SessionResponse {

    private UUID id;
    private String deviceName;
    private String platform;
    private String ipAddress;
    private Instant lastActivityAt;
    private Instant createdAt;
    private boolean isCurrent;
}
