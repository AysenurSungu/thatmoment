package com.thatmoment.modules.analytics.dto.response;

public record AnalyticsSummaryResponse(
        AnalyticsPeriodResponse period,
        AnalyticsPlansSummaryResponse plans,
        AnalyticsJournalsSummaryResponse journals,
        AnalyticsRoutinesSummaryResponse routines
) {
}
