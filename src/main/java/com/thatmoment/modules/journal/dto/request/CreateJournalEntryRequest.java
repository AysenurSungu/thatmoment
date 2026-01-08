package com.thatmoment.modules.journal.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateJournalEntryRequest(
        @Size(max = 100)
        String localId,
        @NotNull
        LocalDate entryDate,
        String content,
        @Min(1)
        @Max(5)
        Integer mood,
        List<String> gratitude,
        @NotNull
        Boolean isFavorite,
        List<UUID> tagIds
) {
}
