package com.thatmoment.modules.plan.mapper;

import com.thatmoment.modules.plan.domain.Plan;
import com.thatmoment.modules.plan.dto.response.PlanResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlanMapper {

    PlanResponse toResponse(Plan plan);
}
