package com.thatmoment.modules.routine.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SkipRoutineRequest(
        @NotNull LocalDate date
) {
}
