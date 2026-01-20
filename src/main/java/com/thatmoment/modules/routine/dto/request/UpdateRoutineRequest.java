package com.thatmoment.modules.routine.dto.request;

import com.thatmoment.modules.routine.domain.enums.RoutineType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

public record UpdateRoutineRequest(
        @NotBlank
        @Size(max = 120)
        String title,
        @Size(max = 1000)
        String description,
        @NotNull
        RoutineType type,
        Integer targetValue,
        String unit,
        @NotNull
        @Valid
        RoutineScheduleRequest schedule,
        LocalDate startDate,
        LocalDate endDate,
        Boolean isActive
) {
    @AssertTrue(message = "endDate must be after startDate")
    public boolean isDateRangeValid() {
        return startDate == null || endDate == null || !endDate.isBefore(startDate);
    }

    @AssertTrue(message = "Invalid target value or unit for routine type")
    public boolean isTargetValid() {
        if (type == null) {
            return true;
        }
        if (type == RoutineType.CHECK) {
            return targetValue == null && !StringUtils.hasText(unit);
        }
        return targetValue != null && targetValue > 0 && StringUtils.hasText(unit);
    }
}
