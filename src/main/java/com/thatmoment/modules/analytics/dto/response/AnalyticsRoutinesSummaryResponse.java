package com.thatmoment.modules.analytics.dto.response;

public record AnalyticsRoutinesSummaryResponse(
        long active,
        long completedDays
) {
}
