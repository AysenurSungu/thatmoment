package com.thatmoment.modules.plan.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record PlanResponse(
        UUID id,
        String title,
        String description,
        LocalDate planDate,
        LocalTime startTime,
        LocalTime endTime,
        String color
) {
}
