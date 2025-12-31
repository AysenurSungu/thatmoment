package com.thatmoment.modules.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
public abstract class SoftDeletableEntity extends BaseEntity {

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    @Column(name = "delete_reason", length = 500)
    private String deleteReason;

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public UUID getDeletedBy() {
        return deletedBy;
    }

    public String getDeleteReason() {
        return deleteReason;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void softDelete(UUID deletedBy, String reason) {
        if (deletedBy == null) {
            throw new IllegalArgumentException("deletedBy cannot be null");
        }
        if (this.deletedAt == null) {
            this.deletedAt = Instant.now();
        }
        this.deletedBy = deletedBy;

        if (reason != null && !reason.isBlank()) {
            this.deleteReason = reason;
        }
    }

    public void restore() {
        this.deletedAt = null;
        this.deletedBy = null;
        this.deleteReason = null;
    }
}
