ALTER TABLE profile.user_preferences
    ADD COLUMN journal_lock_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN journal_password_hash VARCHAR(255);
