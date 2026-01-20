package com.thatmoment.modules.routine.domain;

import com.thatmoment.common.entity.SoftDeletableEntity;
import com.thatmoment.modules.routine.domain.enums.RoutineType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "routines", schema = "routine")
public class Routine extends SoftDeletableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "title", nullable = false, length = 120)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private RoutineType type;

    @Column(name = "target_value")
    private Integer targetValue;

    @Column(name = "unit", length = 50)
    private String unit;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    protected Routine() {
    }

    private Routine(Builder builder) {
        this.userId = builder.userId;
        this.title = builder.title;
        this.description = builder.description;
        this.type = builder.type;
        this.targetValue = builder.targetValue;
        this.unit = builder.unit;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.isActive = builder.isActive != null ? builder.isActive : true;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public RoutineType getType() {
        return type;
    }

    public Integer getTargetValue() {
        return targetValue;
    }

    public String getUnit() {
        return unit;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void updateDetails(
            String title,
            String description,
            RoutineType type,
            Integer targetValue,
            String unit,
            LocalDate startDate,
            LocalDate endDate,
            Boolean isActive
    ) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.targetValue = targetValue;
        this.unit = unit;
        this.startDate = startDate;
        this.endDate = endDate;
        if (isActive != null) {
            this.isActive = isActive;
        }
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public static final class Builder {
        private UUID userId;
        private String title;
        private String description;
        private RoutineType type;
        private Integer targetValue;
        private String unit;
        private LocalDate startDate;
        private LocalDate endDate;
        private Boolean isActive;

        private Builder() {
        }

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder type(RoutineType type) {
            this.type = type;
            return this;
        }

        public Builder targetValue(Integer targetValue) {
            this.targetValue = targetValue;
            return this;
        }

        public Builder unit(String unit) {
            this.unit = unit;
            return this;
        }

        public Builder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Routine build() {
            return new Routine(this);
        }
    }
}
