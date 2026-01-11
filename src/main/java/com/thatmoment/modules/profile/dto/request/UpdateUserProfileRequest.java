package com.thatmoment.modules.profile.dto.request;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateUserProfileRequest(
        @Size(max = 100)
        String name,
        @Size(max = 500)
        String avatarUrl,
        LocalDate dateOfBirth
) {
}
