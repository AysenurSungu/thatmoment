package com.thatmoment.modules.profile.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record UserProfileResponse(
        UUID userId,
        String name,
        String avatarUrl,
        LocalDate dateOfBirth
) {
}
