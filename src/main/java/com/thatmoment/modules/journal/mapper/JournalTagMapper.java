package com.thatmoment.modules.journal.mapper;

import com.thatmoment.modules.journal.domain.JournalTag;
import com.thatmoment.modules.journal.dto.response.JournalTagResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JournalTagMapper {

    JournalTagResponse toResponse(JournalTag tag);
}
