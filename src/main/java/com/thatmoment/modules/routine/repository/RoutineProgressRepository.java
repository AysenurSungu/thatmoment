package com.thatmoment.modules.routine.repository;

import com.thatmoment.modules.routine.domain.RoutineProgress;
import com.thatmoment.modules.routine.domain.enums.ProgressStatus;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
