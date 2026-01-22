package com.thatmoment.modules.journal.repository;

import com.thatmoment.modules.journal.domain.JournalEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {

    Optional<JournalEntry> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    Page<JournalEntry> findByUserIdAndDeletedAtIsNull(UUID userId, Pageable pageable);

    Page<JournalEntry> findByUserIdAndEntryDateAndDeletedAtIsNull(UUID userId, LocalDate entryDate, Pageable pageable);

    long countByUserIdAndEntryDateBetweenAndDeletedAtIsNull(UUID userId, LocalDate from, LocalDate to);

    @Query("""
        select e.mood, count(e)
        from JournalEntry e
        where e.userId = :userId
          and e.deletedAt is null
          and e.entryDate between :from and :to
          and e.mood is not null
        group by e.mood
        """)
    List<Object[]> countMoodsByDateRange(
            @Param("userId") UUID userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}
