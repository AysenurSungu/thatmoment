package com.thatmoment.modules.routine.repository;

import com.thatmoment.modules.routine.domain.RoutineReminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoutineReminderRepository extends JpaRepository<RoutineReminder, UUID> {

    List<RoutineReminder> findByRoutineIdOrderByReminderTime(UUID routineId);

    void deleteByRoutineId(UUID routineId);
}
