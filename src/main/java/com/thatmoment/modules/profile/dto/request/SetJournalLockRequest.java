package com.thatmoment.modules.profile.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SetJournalLockRequest(
        @NotNull
        Boolean enabled,
        @Size(min = 6, max = 128)
        String password
) {
    @AssertTrue(message = "password is required when journal lock is enabled")
    public boolean isPasswordValid() {
        return enabled == null || !enabled || (password != null && !password.isBlank());
    }
}
