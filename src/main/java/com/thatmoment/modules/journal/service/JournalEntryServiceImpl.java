package com.thatmoment.modules.journal.service;

import com.thatmoment.common.exception.exceptions.NotFoundException;
import com.thatmoment.modules.journal.domain.JournalEntry;
import com.thatmoment.modules.journal.domain.JournalEntryTag;
import com.thatmoment.modules.journal.domain.JournalTag;
import com.thatmoment.modules.journal.domain.enums.MoodType;
import com.thatmoment.modules.journal.dto.request.CreateJournalEntryRequest;
import com.thatmoment.modules.journal.dto.request.UpdateJournalEntryRequest;
import com.thatmoment.modules.journal.dto.response.JournalEntryResponse;
import com.thatmoment.modules.journal.mapper.JournalEntryMapper;
import com.thatmoment.modules.journal.repository.JournalEntryRepository;
import com.thatmoment.modules.journal.repository.JournalEntryTagRepository;
import com.thatmoment.modules.journal.repository.JournalTagRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
class JournalEntryServiceImpl implements JournalEntryService {

    private final JournalEntryRepository entryRepository;
    private final JournalEntryTagRepository entryTagRepository;
    private final JournalTagRepository tagRepository;
    private final JournalEntryMapper entryMapper;

    JournalEntryServiceImpl(
            JournalEntryRepository entryRepository,
            JournalEntryTagRepository entryTagRepository,
            JournalTagRepository tagRepository,
            JournalEntryMapper entryMapper
    ) {
        this.entryRepository = entryRepository;
        this.entryTagRepository = entryTagRepository;
        this.tagRepository = tagRepository;
        this.entryMapper = entryMapper;
    }

    @Transactional
    public JournalEntryResponse createEntry(UUID userId, CreateJournalEntryRequest request) {
        List<UUID> tagIds = normalizeTagIds(request.tagIds());
        List<JournalTag> tags = loadTags(userId, tagIds);

        JournalEntry entry = JournalEntry.builder()
                .userId(userId)
                .localId(request.localId())
                .entryDate(request.entryDate())
                .content(request.content())
                .mood(request.mood())
                .gratitude(toArray(request.gratitude()))
                .favorite(Boolean.TRUE.equals(request.isFavorite()))
                .build();

        JournalEntry savedEntry = entryRepository.save(entry);
        linkTags(savedEntry.getId(), tagIds);
        tags.forEach(tag -> tag.adjustUsage(1));

        return entryMapper.toResponse(savedEntry, tagIds);
    }

    @Transactional(readOnly = true)
    public JournalEntryResponse getEntry(UUID userId, UUID entryId) {
        JournalEntry entry = getEntryEntity(userId, entryId);
        List<UUID> tagIds = getTagIds(entry.getId());
        return entryMapper.toResponse(entry, tagIds);
    }

    @Transactional(readOnly = true)
    public Page<JournalEntryResponse> listEntries(UUID userId, LocalDate date, Pageable pageable) {
        Page<JournalEntry> entries;
        if (date != null) {
            entries = entryRepository.findByUserIdAndEntryDateAndDeletedAtIsNull(userId, date, pageable);
        } else {
            entries = entryRepository.findByUserIdAndDeletedAtIsNull(userId, pageable);
        }

        List<JournalEntry> content = entries.getContent();
        Map<UUID, List<UUID>> tagIdsByEntry = getTagIds(content);
        List<JournalEntryResponse> responses = content.stream()
                .map(entry -> entryMapper.toResponse(
                        entry,
                        tagIdsByEntry.getOrDefault(entry.getId(), List.of())
                ))
                .toList();

        return new PageImpl<>(responses, pageable, entries.getTotalElements());
    }

    @Transactional
    public JournalEntryResponse updateEntry(UUID userId, UUID entryId, UpdateJournalEntryRequest request) {
        JournalEntry entry = getEntryEntity(userId, entryId);

        List<UUID> newTagIds = normalizeTagIds(request.tagIds());
        List<UUID> existingTagIds = getTagIds(entryId);
        Set<UUID> existingSet = new HashSet<>(existingTagIds);
        Set<UUID> desiredSet = new HashSet<>(newTagIds);

        Set<UUID> toAdd = new HashSet<>(desiredSet);
        toAdd.removeAll(existingSet);
        Set<UUID> toRemove = new HashSet<>(existingSet);
        toRemove.removeAll(desiredSet);

        List<JournalTag> desiredTags = loadTags(userId, newTagIds);
        updateTagLinks(entryId, toAdd, toRemove);
        adjustUsageCounts(userId, toAdd, toRemove, desiredTags);

        entry.updateDetails(
                request.localId(),
                request.entryDate(),
                request.content(),
                request.mood(),
                toArray(request.gratitude()),
                Boolean.TRUE.equals(request.isFavorite())
        );

        return entryMapper.toResponse(entry, newTagIds);
    }

    @Transactional
    public void deleteEntry(UUID userId, UUID entryId) {
        JournalEntry entry = getEntryEntity(userId, entryId);
        List<UUID> tagIds = getTagIds(entryId);
        if (!tagIds.isEmpty()) {
            List<JournalTag> tags = loadTags(userId, tagIds);
            tags.forEach(tag -> tag.adjustUsage(-1));
            entryTagRepository.deleteByEntryId(entryId);
        }
        entry.softDelete(userId, null);
    }

    @Transactional(readOnly = true)
    public long countEntries(UUID userId, LocalDate from, LocalDate to) {
        return entryRepository.countByUserIdAndEntryDateBetweenAndDeletedAtIsNull(userId, from, to);
    }

    @Transactional(readOnly = true)
    public Map<MoodType, Long> countMoods(UUID userId, LocalDate from, LocalDate to) {
        Map<MoodType, Long> counts = initializeMoodCounts();
        for (Object[] row : entryRepository.countMoodsByDateRange(userId, from, to)) {
            MoodType mood = (MoodType) row[0];
            Number count = (Number) row[1];
            if (mood != null) {
                counts.put(mood, count.longValue());
            }
        }
        return counts;
    }

    private JournalEntry getEntryEntity(UUID userId, UUID entryId) {
        return entryRepository.findByIdAndUserIdAndDeletedAtIsNull(entryId, userId)
                .orElseThrow(() -> new NotFoundException("Entry not found"));
    }

    private List<UUID> normalizeTagIds(List<UUID> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(new LinkedHashSet<>(tagIds));
    }

    private List<JournalTag> loadTags(UUID userId, Collection<UUID> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return List.of();
        }
        List<JournalTag> tags = tagRepository.findByUserIdAndIdInAndDeletedAtIsNull(userId, tagIds);
        if (tags.size() != tagIds.size()) {
            throw new NotFoundException("Tag not found");
        }
        return tags;
    }

    private void linkTags(UUID entryId, List<UUID> tagIds) {
        if (tagIds.isEmpty()) {
            return;
        }
        List<JournalEntryTag> links = tagIds.stream()
                .map(tagId -> new JournalEntryTag(entryId, tagId))
                .toList();
        entryTagRepository.saveAll(links);
    }

    private void updateTagLinks(UUID entryId, Set<UUID> toAdd, Set<UUID> toRemove) {
        if (!toRemove.isEmpty()) {
            entryTagRepository.deleteByEntryIdAndTagIdIn(entryId, toRemove);
        }
        if (!toAdd.isEmpty()) {
            List<JournalEntryTag> links = toAdd.stream()
                    .map(tagId -> new JournalEntryTag(entryId, tagId))
                    .toList();
            entryTagRepository.saveAll(links);
        }
    }

    private void adjustUsageCounts(
            UUID userId,
            Set<UUID> toAdd,
            Set<UUID> toRemove,
            List<JournalTag> desiredTags
    ) {
        if (!toAdd.isEmpty()) {
            Map<UUID, JournalTag> desiredMap = desiredTags.stream()
                    .collect(Collectors.toMap(JournalTag::getId, tag -> tag));
            toAdd.forEach(tagId -> {
                JournalTag tag = desiredMap.get(tagId);
                if (tag != null) {
                    tag.adjustUsage(1);
                }
            });
        }
        if (!toRemove.isEmpty()) {
            List<JournalTag> removedTags = loadTags(userId, toRemove);
            removedTags.forEach(tag -> tag.adjustUsage(-1));
        }
    }

    private List<UUID> getTagIds(UUID entryId) {
        return entryTagRepository.findByEntryId(entryId).stream()
                .map(JournalEntryTag::getTagId)
                .toList();
    }

    private Map<UUID, List<UUID>> getTagIds(List<JournalEntry> entries) {
        if (entries.isEmpty()) {
            return Map.of();
        }
        List<UUID> entryIds = entries.stream()
                .map(JournalEntry::getId)
                .toList();
        List<JournalEntryTag> links = entryTagRepository.findByEntryIdIn(entryIds);
        Map<UUID, List<UUID>> map = new HashMap<>();
        for (JournalEntryTag link : links) {
            map.computeIfAbsent(link.getEntryId(), key -> new ArrayList<>())
                    .add(link.getTagId());
        }
        return map;
    }

    private String[] toArray(List<String> gratitude) {
        if (gratitude == null || gratitude.isEmpty()) {
            return null;
        }
        return gratitude.toArray(new String[0]);
    }

    private Map<MoodType, Long> initializeMoodCounts() {
        Map<MoodType, Long> counts = new EnumMap<>(MoodType.class);
        for (MoodType mood : MoodType.values()) {
            counts.put(mood, 0L);
        }
        return counts;
    }
}
