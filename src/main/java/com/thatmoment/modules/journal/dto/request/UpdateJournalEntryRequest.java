package com.thatmoment.modules.journal.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.thatmoment.modules.journal.domain.enums.MoodType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UpdateJournalEntryRequest(
        @Size(max = 100)
        String localId,
        @NotNull
        LocalDate entryDate,
        String content,
        MoodType mood,
        List<String> gratitude,
        @NotNull
        Boolean isFavorite,
        List<UUID> tagIds
) {
}
