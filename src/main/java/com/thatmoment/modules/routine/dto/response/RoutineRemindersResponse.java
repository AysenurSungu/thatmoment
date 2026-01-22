package com.thatmoment.modules.routine.dto.response;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record RoutineRemindersResponse(
        UUID routineId,
        List<LocalTime> times
) {
}
