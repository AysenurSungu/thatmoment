package com.thatmoment.modules.routine.domain;

import com.thatmoment.common.entity.BaseEntity;
import com.thatmoment.modules.routine.domain.enums.RoutineDayOfWeek;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "routine_schedules", schema = "routine")
public class RoutineSchedule extends BaseEntity {

    @Column(name = "routine_id", nullable = false)
    private UUID routineId;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private RoutineDayOfWeek dayOfWeek;

    protected RoutineSchedule() {
    }

    public RoutineSchedule(UUID routineId, RoutineDayOfWeek dayOfWeek) {
        this.routineId = routineId;
        this.dayOfWeek = dayOfWeek;
    }

    public UUID getRoutineId() {
        return routineId;
    }

    public RoutineDayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }
}
