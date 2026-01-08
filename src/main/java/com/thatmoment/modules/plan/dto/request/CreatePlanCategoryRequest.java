package com.thatmoment.modules.plan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePlanCategoryRequest(
        @NotBlank
        @Size(max = 50)
        String name,
        @NotBlank
        @Size(max = 7)
        String color,
        @Size(max = 50)
        String icon,
        @NotNull
        Boolean isDefault
) {
}
