package com.thatmoment.modules.routine.dto.response;

public record RoutineSummaryResponse(
        double completionRate,
        int currentStreak,
        int longestStreak,
        int completedDays,
        int totalTargetDays
) {
}
