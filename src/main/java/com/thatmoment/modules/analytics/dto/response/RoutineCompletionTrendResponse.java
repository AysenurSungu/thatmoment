package com.thatmoment.modules.analytics.dto.response;

import java.time.LocalDate;

public record RoutineCompletionTrendResponse(
        LocalDate date,
        long completedDays
) {
}
