package com.thatmoment.modules.journal.repository;

import com.thatmoment.modules.journal.domain.JournalEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {

    Optional<JournalEntry> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    Page<JournalEntry> findByUserIdAndDeletedAtIsNull(UUID userId, Pageable pageable);

    Page<JournalEntry> findByUserIdAndEntryDateAndDeletedAtIsNull(UUID userId, LocalDate entryDate, Pageable pageable);
}
