package com.thatmoment.modules.routine.repository;

import com.thatmoment.modules.routine.domain.RoutineProgress;
import com.thatmoment.modules.routine.domain.enums.ProgressStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoutineProgressRepository extends JpaRepository<RoutineProgress, UUID> {

    Optional<RoutineProgress> findByRoutineIdAndProgressDate(UUID routineId, LocalDate progressDate);

    List<RoutineProgress> findByRoutineIdAndProgressDateBetweenOrderByProgressDate(
            UUID routineId,
            LocalDate from,
            LocalDate to
    );

    long countByUserIdAndProgressDateAndStatus(UUID userId, LocalDate date, ProgressStatus status);

    long countByUserIdAndProgressDateBetweenAndStatus(
            UUID userId,
            LocalDate from,
            LocalDate to,
            ProgressStatus status
    );

    @Query("""
        select p.progressDate, count(p)
        from RoutineProgress p
        where p.userId = :userId
          and p.status = :status
          and p.progressDate between :from and :to
        group by p.progressDate
        """)
    List<Object[]> countCompletedByDate(
            @Param("userId") UUID userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("status") ProgressStatus status
    );
}
