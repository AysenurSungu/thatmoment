package com.thatmoment.auth.repository;

import com.thatmoment.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    @Query("select u from User u where u.email = :email and u.isActive = true and u.isVerified = true and u.deletedAt is null")
    Optional<User> findActiveVerifiedUserByEmail(@Param("email") String email);
}
