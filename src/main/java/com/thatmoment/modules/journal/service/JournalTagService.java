package com.thatmoment.modules.journal.service;

import com.thatmoment.modules.journal.dto.request.CreateJournalTagRequest;
import com.thatmoment.modules.journal.dto.request.UpdateJournalTagRequest;
import com.thatmoment.modules.journal.dto.response.JournalTagResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface JournalTagService {

    JournalTagResponse createTag(UUID userId, CreateJournalTagRequest request);

    JournalTagResponse getTag(UUID userId, UUID tagId);

    Page<JournalTagResponse> listTags(UUID userId, Pageable pageable);

    JournalTagResponse updateTag(UUID userId, UUID tagId, UpdateJournalTagRequest request);

    void deleteTag(UUID userId, UUID tagId);
}
