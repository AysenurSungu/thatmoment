package com.thatmoment.modules.plan.domain;

import com.thatmoment.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "categories", schema = "calendar")
public class PlanCategory extends SoftDeletableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "color", nullable = false, length = 7)
    private String color;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "is_default")
    private boolean isDefault;

    protected PlanCategory() {
    }

    private PlanCategory(Builder builder) {
        this.userId = builder.userId;
        this.name = builder.name;
        this.color = builder.color;
        this.icon = builder.icon;
        this.isDefault = builder.isDefault;
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

    public String getIcon() {
        return icon;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void updateDetails(String name, String color, String icon, boolean isDefault) {
        this.name = name;
        this.color = color;
        this.icon = icon;
        this.isDefault = isDefault;
    }

    public static final class Builder {
        private UUID userId;
        private String name;
        private String color;
        private String icon;
        private boolean isDefault;

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

        public Builder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public Builder isDefault(boolean isDefault) {
            this.isDefault = isDefault;
            return this;
        }

        public PlanCategory build() {
            return new PlanCategory(this);
        }
    }
}
