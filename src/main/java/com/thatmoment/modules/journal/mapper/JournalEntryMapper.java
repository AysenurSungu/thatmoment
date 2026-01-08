package com.thatmoment.modules.journal.mapper;

import com.thatmoment.modules.journal.domain.JournalEntry;
import com.thatmoment.modules.journal.dto.response.JournalEntryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JournalEntryMapper {

    @Mapping(target = "tagIds", source = "tagIds")
    @Mapping(target = "isFavorite", source = "entry.favorite")
    JournalEntryResponse toResponse(JournalEntry entry, List<UUID> tagIds);
}
