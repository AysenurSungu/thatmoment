package com.thatmoment.modules.plan.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record UpdatePlanRequest(
        @NotBlank
        @Size(max = 120)
        String title,
        @Size(max = 1000)
        String description,
        @NotNull
        LocalDate planDate,
        @NotNull
        LocalTime startTime,
        @NotNull
        LocalTime endTime,
        @Size(max = 20)
        String color
) {
    @AssertTrue(message = "endTime must be after startTime")
    public boolean isTimeRangeValid() {
        return startTime == null || endTime == null || endTime.isAfter(startTime);
    }
}
