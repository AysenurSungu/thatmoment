package com.thatmoment.modules.routine.domain;

import com.thatmoment.common.entity.BaseEntity;
import com.thatmoment.modules.routine.domain.enums.ProgressStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "routine_progress", schema = "routine")
public class RoutineProgress extends BaseEntity {

    @Column(name = "routine_id", nullable = false)
    private UUID routineId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "progress_date", nullable = false)
    private LocalDate progressDate;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProgressStatus status;

    protected RoutineProgress() {
    }

    private RoutineProgress(Builder builder) {
        this.routineId = builder.routineId;
        this.userId = builder.userId;
        this.progressDate = builder.progressDate;
        this.amount = builder.amount;
        this.status = builder.status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getRoutineId() {
        return routineId;
    }

    public UUID getUserId() {
        return userId;
    }

    public LocalDate getProgressDate() {
        return progressDate;
    }

    public Integer getAmount() {
        return amount;
    }

    public ProgressStatus getStatus() {
        return status;
    }

    public void updateProgress(Integer amount, ProgressStatus status) {
        this.amount = amount;
        this.status = status;
    }

    public static final class Builder {
        private UUID routineId;
        private UUID userId;
        private LocalDate progressDate;
        private Integer amount;
        private ProgressStatus status;

        private Builder() {
        }

        public Builder routineId(UUID routineId) {
            this.routineId = routineId;
            return this;
        }

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder progressDate(LocalDate progressDate) {
            this.progressDate = progressDate;
            return this;
        }

        public Builder amount(Integer amount) {
            this.amount = amount;
            return this;
        }

        public Builder status(ProgressStatus status) {
            this.status = status;
            return this;
        }

        public RoutineProgress build() {
            return new RoutineProgress(this);
        }
    }
}
