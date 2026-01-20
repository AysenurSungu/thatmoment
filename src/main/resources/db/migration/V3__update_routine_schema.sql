-- =====================================================
-- ROUTINE SCHEMA UPDATE - Routine module alignment
-- =====================================================

-- Routine definitions
ALTER TABLE routine.routines
    RENAME COLUMN name TO title;

ALTER TABLE routine.routines
    ALTER COLUMN title TYPE VARCHAR(120);

ALTER TABLE routine.routines
    RENAME COLUMN daily_target TO target_value;

ALTER TABLE routine.routines
    ALTER COLUMN target_value DROP NOT NULL,
    ALTER COLUMN target_value DROP DEFAULT;

ALTER TABLE routine.routines
    ALTER COLUMN unit DROP NOT NULL;

ALTER TABLE routine.routines
    ALTER COLUMN category DROP NOT NULL;

ALTER TABLE routine.routines
    ADD COLUMN type VARCHAR(20);

UPDATE routine.routines
SET type = 'NUMERIC'
WHERE type IS NULL;

ALTER TABLE routine.routines
    ALTER COLUMN type SET NOT NULL;

ALTER TABLE routine.routines
    ADD COLUMN start_date DATE,
    ADD COLUMN end_date DATE;

ALTER TABLE routine.routines
    ADD CONSTRAINT routine_type_check
        CHECK (type IN ('CHECK', 'NUMERIC')),
    ADD CONSTRAINT routine_dates_check
        CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date),
    ADD CONSTRAINT routine_target_value_check
        CHECK (
            (type = 'CHECK' AND target_value IS NULL AND unit IS NULL)
            OR (type = 'NUMERIC' AND target_value IS NOT NULL AND target_value > 0 AND unit IS NOT NULL)
        );

CREATE INDEX IF NOT EXISTS idx_routines_user_active
    ON routine.routines (user_id, is_active);

CREATE INDEX IF NOT EXISTS idx_routines_user_deleted
    ON routine.routines (user_id, deleted_at);

-- Routine schedules
CREATE TABLE routine.routine_schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    routine_id UUID NOT NULL REFERENCES routine.routines(id) ON DELETE CASCADE,
    day_of_week VARCHAR(10) NOT NULL,

    -- BaseEntity fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID,

    UNIQUE (routine_id, day_of_week),
    CONSTRAINT routine_schedule_day_check
        CHECK (day_of_week IN ('MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN'))
);

CREATE INDEX IF NOT EXISTS idx_routine_schedules_routine
    ON routine.routine_schedules (routine_id);

-- Routine progress
CREATE TABLE routine.routine_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    routine_id UUID NOT NULL REFERENCES routine.routines(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    progress_date DATE NOT NULL,
    amount INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,

    -- BaseEntity fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID,

    UNIQUE (routine_id, progress_date),
    CONSTRAINT routine_progress_status_check
        CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'SKIPPED'))
);

CREATE INDEX IF NOT EXISTS idx_routine_progress_user_date
    ON routine.routine_progress (user_id, progress_date);

CREATE INDEX IF NOT EXISTS idx_routine_progress_routine_date
    ON routine.routine_progress (routine_id, progress_date);
