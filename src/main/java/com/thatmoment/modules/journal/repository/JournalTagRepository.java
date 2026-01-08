package com.thatmoment.modules.journal.repository;

import com.thatmoment.modules.journal.domain.JournalTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JournalTagRepository extends JpaRepository<JournalTag, UUID> {

    Optional<JournalTag> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    Page<JournalTag> findByUserIdAndDeletedAtIsNull(UUID userId, Pageable pageable);

    boolean existsByUserIdAndNameAndDeletedAtIsNull(UUID userId, String name);

    List<JournalTag> findByUserIdAndIdInAndDeletedAtIsNull(UUID userId, Collection<UUID> ids);
}
