package com.thatmoment.modules.plan.mapper;

import com.thatmoment.modules.plan.domain.PlanCategory;
import com.thatmoment.modules.plan.dto.response.PlanCategoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlanCategoryMapper {

    PlanCategoryResponse toResponse(PlanCategory category);
}
