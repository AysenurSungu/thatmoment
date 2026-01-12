package com.thatmoment.modules.profile.domain;

import com.thatmoment.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_profiles", schema = "profile")
public class UserProfile extends SoftDeletableEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    protected UserProfile() {
    }

    private UserProfile(Builder builder) {
        this.userId = builder.userId;
        this.name = builder.name;
        this.avatarUrl = builder.avatarUrl;
        this.dateOfBirth = builder.dateOfBirth;
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

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void updateDetails(String name, String avatarUrl, LocalDate dateOfBirth) {
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.dateOfBirth = dateOfBirth;
    }

    public static final class Builder {
        private UUID userId;
        private String name;
        private String avatarUrl;
        private LocalDate dateOfBirth;

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

        public Builder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public Builder dateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public UserProfile build() {
            return new UserProfile(this);
        }
    }
}
