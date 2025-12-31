package com.thatmoment.modules.auth.repository;

import com.thatmoment.modules.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHashAndIsActiveTrue(String tokenHash);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findBySessionIdAndIsActiveTrue(UUID sessionId);

    @Modifying
    @Query("update RefreshToken rt " +
            "set rt.isActive = false " +
            "where rt.sessionId = :sessionId and rt.isActive = true")
    int revokeAllBySessionId(@Param("sessionId") UUID sessionId);

    @Modifying
    @Query("update RefreshToken rt " +
            "set rt.isActive = false " +
            "where rt.sessionId in (select s.id from Session s where s.userId = :userId) " +
            "and rt.isActive = true")
    int revokeAllByUserId(@Param("userId") UUID userId);
}
