package com.thatmoment.modules.routine.mapper;

import com.thatmoment.modules.routine.domain.Routine;
import com.thatmoment.modules.routine.dto.response.RoutineResponse;
import com.thatmoment.modules.routine.dto.response.RoutineScheduleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoutineMapper {

    @Mapping(target = "schedule", source = "schedule")
    RoutineResponse toResponse(Routine routine, RoutineScheduleResponse schedule);
}
