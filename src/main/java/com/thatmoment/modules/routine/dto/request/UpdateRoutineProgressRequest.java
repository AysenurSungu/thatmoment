package com.thatmoment.modules.routine.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateRoutineProgressRequest(
        @NotNull
        Integer amount
) {
}
