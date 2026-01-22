package com.thatmoment.modules.routine.domain;

import com.thatmoment.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "routine_reminders", schema = "routine")
public class RoutineReminder extends BaseEntity {

    @Column(name = "routine_id", nullable = false)
    private UUID routineId;

    @Column(name = "reminder_time", nullable = false)
    private LocalTime reminderTime;

    protected RoutineReminder() {
    }

    public RoutineReminder(UUID routineId, LocalTime reminderTime) {
        this.routineId = routineId;
        this.reminderTime = reminderTime;
    }

    public UUID getRoutineId() {
        return routineId;
    }

    public LocalTime getReminderTime() {
        return reminderTime;
    }
}
