package com.thatmoment.modules.routine.dto.response;

import com.thatmoment.modules.routine.domain.enums.RoutineType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record RoutineResponse(
        UUID id,
        String title,
        String description,
        RoutineType type,
        Integer targetValue,
        String unit,
        RoutineScheduleResponse schedule,
        LocalDate startDate,
        LocalDate endDate,
        Boolean isActive,
        Instant createdAt
) {
}
