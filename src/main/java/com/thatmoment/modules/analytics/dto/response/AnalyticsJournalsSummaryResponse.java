package com.thatmoment.modules.analytics.dto.response;

import com.thatmoment.modules.journal.domain.enums.MoodType;

import java.util.Map;

public record AnalyticsJournalsSummaryResponse(
        long entries,
        Map<MoodType, Long> moods
) {
}
