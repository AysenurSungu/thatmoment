package com.thatmoment.modules.routine.dto.response;

public record RoutineOverviewResponse(
        int todayRequiredCount,
        int todayCompletedCount,
        int activeRoutineCount
) {
}
