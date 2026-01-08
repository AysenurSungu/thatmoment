package com.thatmoment.modules.journal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateJournalTagRequest(
        @NotBlank
        @Size(max = 50)
        String name,
        @NotBlank
        @Size(max = 7)
        String color
) {
}
