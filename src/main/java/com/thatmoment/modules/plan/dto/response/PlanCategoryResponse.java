package com.thatmoment.modules.plan.dto.response;

import java.util.UUID;

public record PlanCategoryResponse(
        UUID id,
        String name,
        String color,
        String icon,
        boolean isDefault
) {
}
