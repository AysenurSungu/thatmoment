package com.thatmoment.modules.profile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyJournalLockRequest(
        @NotBlank
        @Size(min = 6, max = 128)
        String password
) {
}
