package com.thatmoment.modules.routine.repository;

import com.thatmoment.modules.routine.domain.RoutineSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoutineScheduleRepository extends JpaRepository<RoutineSchedule, UUID> {

    List<RoutineSchedule> findByRoutineId(UUID routineId);

    List<RoutineSchedule> findByRoutineIdIn(List<UUID> routineIds);

    void deleteByRoutineId(UUID routineId);
}
