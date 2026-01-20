package com.thatmoment.modules.routine.dto.request;

import com.thatmoment.modules.routine.domain.enums.RoutineDayOfWeek;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record RoutineScheduleRequest(
        @NotEmpty
        List<RoutineDayOfWeek> daysOfWeek
) {
}
