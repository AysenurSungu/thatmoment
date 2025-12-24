package com.thatmoment.auth.repository;

import com.thatmoment.auth.domain.EmailVerification;
import com.thatmoment.auth.domain.enums.VerificationPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, UUID> {

    @Query("select ev from EmailVerification ev " +
            "where ev.userId = :userId " +
            "and ev.purpose = :purpose " +
            "and ev.verifiedAt is null " +
            "and ev.expiresAt > :now " +
            "and ev.attemptCount < ev.maxAttempts " +
            "order by ev.createdAt desc")
    List<EmailVerification> findActiveVerification(
            @Param("userId") UUID userId,
            @Param("purpose") VerificationPurpose purpose,
            @Param("now") Instant now,
            Pageable pageable
    );

    @Modifying
    @Query("update EmailVerification ev " +
            "set ev.expiresAt = :now " +
            "where ev.userId = :userId " +
            "and ev.purpose = :purpose " +
            "and ev.verifiedAt is null " +
            "and ev.expiresAt > :now")
    int invalidatePendingVerifications(
            @Param("userId") UUID userId,
            @Param("purpose") VerificationPurpose purpose,
            @Param("now") Instant now
    );
}
