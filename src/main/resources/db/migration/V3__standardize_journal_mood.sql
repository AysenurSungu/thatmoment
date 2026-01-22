DO $$
BEGIN
    IF (
        SELECT data_type
        FROM information_schema.columns
        WHERE table_schema = 'journal'
          AND table_name = 'entries'
          AND column_name = 'mood'
    ) = 'integer' THEN
        ALTER TABLE journal.entries
            ALTER COLUMN mood TYPE VARCHAR(20)
            USING CASE mood
                WHEN 1 THEN 'ANGRY'
                WHEN 2 THEN 'SAD'
                WHEN 3 THEN 'BORED'
                WHEN 4 THEN 'CALM'
                WHEN 5 THEN 'HAPPY'
                ELSE NULL
            END;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'journal_entries_mood_check'
          AND conrelid = 'journal.entries'::regclass
    ) THEN
        ALTER TABLE journal.entries
            ADD CONSTRAINT journal_entries_mood_check
                CHECK (mood IN ('HAPPY', 'SAD', 'BORED', 'ANGRY', 'CALM'));
    END IF;
END $$;
