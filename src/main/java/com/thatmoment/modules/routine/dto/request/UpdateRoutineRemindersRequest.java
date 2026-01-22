package com.thatmoment.modules.routine.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.List;

public record UpdateRoutineRemindersRequest(
        @NotNull List<LocalTime> times
) {
}
