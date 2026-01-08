package com.thatmoment.modules.journal.dto.response;

import java.util.UUID;

public record JournalTagResponse(
        UUID id,
        String name,
        String color,
        int usageCount
) {
}
