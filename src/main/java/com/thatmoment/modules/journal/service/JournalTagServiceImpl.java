package com.thatmoment.modules.journal.service;

import com.thatmoment.common.exception.exceptions.ConflictException;
import com.thatmoment.common.exception.exceptions.NotFoundException;
import com.thatmoment.modules.journal.domain.JournalTag;
import com.thatmoment.modules.journal.dto.request.CreateJournalTagRequest;
import com.thatmoment.modules.journal.dto.request.UpdateJournalTagRequest;
import com.thatmoment.modules.journal.dto.response.JournalTagResponse;
import com.thatmoment.modules.journal.mapper.JournalTagMapper;
import com.thatmoment.modules.journal.repository.JournalTagRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
class JournalTagServiceImpl implements JournalTagService {

    private final JournalTagRepository tagRepository;
    private final JournalTagMapper tagMapper;

    JournalTagServiceImpl(JournalTagRepository tagRepository, JournalTagMapper tagMapper) {
        this.tagRepository = tagRepository;
        this.tagMapper = tagMapper;
    }

    @Transactional
    public JournalTagResponse createTag(UUID userId, CreateJournalTagRequest request) {
        String name = request.name().trim();
        if (tagRepository.existsByUserIdAndNameAndDeletedAtIsNull(userId, name)) {
            throw new ConflictException("Tag name already exists");
        }

        JournalTag tag = JournalTag.builder()
                .userId(userId)
                .name(name)
                .color(request.color())
                .usageCount(0)
                .build();

        return tagMapper.toResponse(tagRepository.save(tag));
    }

    @Transactional(readOnly = true)
    public JournalTagResponse getTag(UUID userId, UUID tagId) {
        return tagMapper.toResponse(getTagEntity(userId, tagId));
    }

    @Transactional(readOnly = true)
    public Page<JournalTagResponse> listTags(UUID userId, Pageable pageable) {
        return tagRepository.findByUserIdAndDeletedAtIsNull(userId, pageable)
                .map(tagMapper::toResponse);
    }

    @Transactional
    public JournalTagResponse updateTag(UUID userId, UUID tagId, UpdateJournalTagRequest request) {
        JournalTag tag = getTagEntity(userId, tagId);
        String name = request.name().trim();

        if (!name.equals(tag.getName())
                && tagRepository.existsByUserIdAndNameAndDeletedAtIsNull(userId, name)) {
            throw new ConflictException("Tag name already exists");
        }

        tag.updateDetails(name, request.color());
        return tagMapper.toResponse(tag);
    }

    @Transactional
    public void deleteTag(UUID userId, UUID tagId) {
        JournalTag tag = getTagEntity(userId, tagId);
        tag.softDelete(userId, null);
    }

    private JournalTag getTagEntity(UUID userId, UUID tagId) {
        return tagRepository.findByIdAndUserIdAndDeletedAtIsNull(tagId, userId)
                .orElseThrow(() -> new NotFoundException("Tag not found"));
    }
}
