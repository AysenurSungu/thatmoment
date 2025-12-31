package com.thatmoment.modules.auth.repository;

import com.thatmoment.modules.auth.domain.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {

    Optional<Session> findByIdAndUserIdAndIsActiveTrue(UUID id, UUID userId);

    List<Session> findByUserIdAndIsActiveTrueOrderByLastActivityAtDesc(UUID userId);

    @Modifying
    @Query("update Session s " +
            "set s.isActive = false, s.revokedAt = :now, s.revokedReason = :reason " +
            "where s.userId = :userId and s.isActive = true")
    int revokeAllByUserId(
            @Param("userId") UUID userId,
            @Param("reason") String reason,
            @Param("now") Instant now
    );
}
