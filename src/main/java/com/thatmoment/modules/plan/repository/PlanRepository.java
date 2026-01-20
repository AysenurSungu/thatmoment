package com.thatmoment.modules.plan.repository;

import com.thatmoment.modules.plan.domain.Plan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
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

    long countByUserIdAndPlanDateBetweenAndDeletedAtIsNull(UUID userId, LocalDate from, LocalDate to);

    long countByUserIdAndPlanDateBetweenAndIsCompletedTrueAndDeletedAtIsNull(UUID userId, LocalDate from, LocalDate to);

    @Query("""
        select p.planDate, count(p)
        from Plan p
        where p.userId = :userId
          and p.deletedAt is null
          and p.planDate between :from and :to
        group by p.planDate
        """)
    List<Object[]> countTotalsByDate(
            @Param("userId") UUID userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
        select p.planDate, count(p)
        from Plan p
        where p.userId = :userId
          and p.deletedAt is null
          and p.isCompleted = true
          and p.planDate between :from and :to
        group by p.planDate
        """)
    List<Object[]> countCompletedByDate(
            @Param("userId") UUID userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}
