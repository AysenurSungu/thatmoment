package com.thatmoment.modules.routine.dto.response;

import com.thatmoment.modules.routine.domain.enums.ProgressStatus;

import java.time.LocalDate;
import java.util.UUID;

public record RoutineProgressResponse(
        UUID routineId,
        LocalDate date,
        Integer amount,
        ProgressStatus status
) {
}
