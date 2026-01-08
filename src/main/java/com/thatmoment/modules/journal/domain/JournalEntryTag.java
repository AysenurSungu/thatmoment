package com.thatmoment.modules.journal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "entry_tags", schema = "journal")
public class JournalEntryTag {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "entry_id", nullable = false)
    private UUID entryId;

    @Column(name = "tag_id", nullable = false)
    private UUID tagId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected JournalEntryTag() {
    }

    public JournalEntryTag(UUID entryId, UUID tagId) {
        this.entryId = entryId;
        this.tagId = tagId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEntryId() {
        return entryId;
    }

    public UUID getTagId() {
        return tagId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
