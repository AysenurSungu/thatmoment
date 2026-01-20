package com.thatmoment.modules.routine.dto.response;

import com.thatmoment.modules.routine.domain.enums.RoutineDayOfWeek;

import java.util.List;

public record RoutineScheduleResponse(
        List<RoutineDayOfWeek> daysOfWeek
) {
}
