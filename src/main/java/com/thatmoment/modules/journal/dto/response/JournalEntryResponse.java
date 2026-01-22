package com.thatmoment.modules.journal.dto.response;

import com.thatmoment.modules.journal.domain.enums.MoodType;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record JournalEntryResponse(
        UUID id,
        String localId,
        LocalDate entryDate,
        String content,
        MoodType mood,
        List<String> gratitude,
        boolean isFavorite,
        int wordCount,
        List<UUID> tagIds
) {
}
