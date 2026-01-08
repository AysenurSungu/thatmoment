package com.thatmoment.modules.journal.domain;

import com.thatmoment.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "entries", schema = "journal")
public class JournalEntry extends SoftDeletableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "local_id", length = 100)
    private String localId;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "content")
    private String content;

    @Column(name = "mood")
    private Integer mood;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "gratitude", columnDefinition = "text[]")
    private String[] gratitude;

    @Column(name = "is_favorite", nullable = false)
    private boolean favorite;

    @Column(name = "word_count", nullable = false)
    private int wordCount;

    protected JournalEntry() {
    }

    private JournalEntry(Builder builder) {
        this.userId = builder.userId;
        this.localId = builder.localId;
        this.entryDate = builder.entryDate;
        this.content = builder.content;
        this.mood = builder.mood;
        this.gratitude = builder.gratitude;
        this.favorite = builder.favorite;
        this.wordCount = countWords(builder.content);
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getUserId() {
        return userId;
    }

    public String getLocalId() {
        return localId;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public String getContent() {
        return content;
    }

    public Integer getMood() {
        return mood;
    }

    public String[] getGratitude() {
        return gratitude;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void updateDetails(
            String localId,
            LocalDate entryDate,
            String content,
            Integer mood,
            String[] gratitude,
            boolean favorite
    ) {
        this.localId = localId;
        this.entryDate = entryDate;
        this.content = content;
        this.mood = mood;
        this.gratitude = gratitude;
        this.favorite = favorite;
        this.wordCount = countWords(content);
    }

    private static int countWords(String content) {
        if (content == null || content.isBlank()) {
            return 0;
        }
        String trimmed = content.trim();
        if (trimmed.isEmpty()) {
            return 0;
        }
        return trimmed.split("\\s+").length;
    }

    public static final class Builder {
        private UUID userId;
        private String localId;
        private LocalDate entryDate;
        private String content;
        private Integer mood;
        private String[] gratitude;
        private boolean favorite;

        private Builder() {
        }

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder localId(String localId) {
            this.localId = localId;
            return this;
        }

        public Builder entryDate(LocalDate entryDate) {
            this.entryDate = entryDate;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder mood(Integer mood) {
            this.mood = mood;
            return this;
        }

        public Builder gratitude(String[] gratitude) {
            this.gratitude = gratitude;
            return this;
        }

        public Builder favorite(boolean favorite) {
            this.favorite = favorite;
            return this;
        }

        public JournalEntry build() {
            return new JournalEntry(this);
        }
    }
}
