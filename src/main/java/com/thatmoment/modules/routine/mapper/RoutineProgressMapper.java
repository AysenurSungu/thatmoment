package com.thatmoment.modules.routine.mapper;

import com.thatmoment.modules.routine.domain.RoutineProgress;
import com.thatmoment.modules.routine.dto.response.RoutineProgressResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoutineProgressMapper {

    @Mapping(source = "progressDate", target = "date")
    RoutineProgressResponse toResponse(RoutineProgress progress);
}
