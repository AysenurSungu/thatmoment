package com.thatmoment.modules.analytics.dto.response;

import java.time.LocalDate;

public record AnalyticsPeriodResponse(
        LocalDate from,
        LocalDate to,
        String weekStart
) {
}
