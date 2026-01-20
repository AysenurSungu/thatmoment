package com.thatmoment.modules.plan.repository;

import com.thatmoment.modules.plan.domain.Plan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlanRepository extends JpaRepository<Plan, UUID> {

    Optional<Plan> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    Page<Plan> findByUserIdAndDeletedAtIsNull(UUID userId, Pageable pageable);

    Page<Plan> findByUserIdAndPlanDateAndDeletedAtIsNull(UUID userId, LocalDate planDate, Pageable pageable);

    Page<Plan> findByUserIdAndIsCompletedAndDeletedAtIsNull(UUID userId, boolean isCompleted, Pageable pageable);

    Page<Plan> findByUserIdAndPlanDateAndIsCompletedAndDeletedAtIsNull(
            UUID userId,
            LocalDate planDate,
            boolean isCompleted,
            Pageable pageable
    );
}
