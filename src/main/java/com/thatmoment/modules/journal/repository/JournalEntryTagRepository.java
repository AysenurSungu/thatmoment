package com.thatmoment.modules.journal.repository;

import com.thatmoment.modules.journal.domain.JournalEntryTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface JournalEntryTagRepository extends JpaRepository<JournalEntryTag, UUID> {

    List<JournalEntryTag> findByEntryId(UUID entryId);

    List<JournalEntryTag> findByEntryIdIn(Collection<UUID> entryIds);

    @Modifying
    @Query("delete from JournalEntryTag et where et.entryId = :entryId and et.tagId in :tagIds")
    void deleteByEntryIdAndTagIdIn(@Param("entryId") UUID entryId, @Param("tagIds") Collection<UUID> tagIds);

    @Modifying
    @Query("delete from JournalEntryTag et where et.entryId = :entryId")
    void deleteByEntryId(@Param("entryId") UUID entryId);
}
