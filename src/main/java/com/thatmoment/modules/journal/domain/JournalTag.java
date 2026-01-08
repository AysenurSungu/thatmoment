package com.thatmoment.modules.journal.domain;

import com.thatmoment.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "tags", schema = "journal")
public class JournalTag extends SoftDeletableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "color", nullable = false, length = 7)
    private String color;

    @Column(name = "usage_count", nullable = false)
    private int usageCount;

    protected JournalTag() {
    }

    private JournalTag(Builder builder) {
        this.userId = builder.userId;
        this.name = builder.name;
        this.color = builder.color;
        this.usageCount = builder.usageCount;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public void updateDetails(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public void adjustUsage(int delta) {
        int next = this.usageCount + delta;
        this.usageCount = Math.max(0, next);
    }

    public static final class Builder {
        private UUID userId;
        private String name;
        private String color;
        private int usageCount;

        private Builder() {
        }

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder color(String color) {
            this.color = color;
            return this;
        }

        public Builder usageCount(int usageCount) {
            this.usageCount = usageCount;
            return this;
        }

        public JournalTag build() {
            return new JournalTag(this);
        }
    }
}
