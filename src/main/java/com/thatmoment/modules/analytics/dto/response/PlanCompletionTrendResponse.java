package com.thatmoment.modules.analytics.dto.response;

import java.time.LocalDate;

public record PlanCompletionTrendResponse(
        LocalDate date,
        long completed,
        long total
) {
}
