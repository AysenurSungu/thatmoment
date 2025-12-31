package com.thatmoment.modules.plan.mapper;

import com.thatmoment.modules.plan.domain.Plan;
import com.thatmoment.modules.plan.dto.response.PlanResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlanMapper {

    PlanResponse toResponse(Plan plan);
}
