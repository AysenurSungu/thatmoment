package com.thatmoment.modules.plan.domain;

import com.thatmoment.modules.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "plans", schema = "plan")
public class Plan extends SoftDeletableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "title", nullable = false, length = 120)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "plan_date", nullable = false)
    private LocalDate planDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "color", length = 20)
    private String color;

    protected Plan() {
    }

    private Plan(Builder builder) {
        this.userId = builder.userId;
        this.title = builder.title;
        this.description = builder.description;
        this.planDate = builder.planDate;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.color = builder.color;
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

    public LocalDate getPlanDate() {
        return planDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getColor() {
        return color;
    }

    public void updateDetails(
            String title,
            String description,
            LocalDate planDate,
            LocalTime startTime,
            LocalTime endTime,
            String color
    ) {
        this.title = title;
        this.description = description;
        this.planDate = planDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.color = color;
    }

    public static final class Builder {
        private UUID userId;
        private String title;
        private String description;
        private LocalDate planDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String color;

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

        public Builder planDate(LocalDate planDate) {
            this.planDate = planDate;
            return this;
        }

        public Builder startTime(LocalTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(LocalTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder color(String color) {
            this.color = color;
            return this;
        }

        public Plan build() {
            return new Plan(this);
        }
    }
}
