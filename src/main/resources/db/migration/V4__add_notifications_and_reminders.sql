-- =====================================================
-- NOTIFICATION + ROUTINE REMINDERS
-- =====================================================

-- Notifications
-- Entity Type: BaseEntity
CREATE TABLE notification.notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    type VARCHAR(30) NOT NULL,
    title VARCHAR(120),
    message VARCHAR(500),
    payload JSONB,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,

    -- BaseEntity fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID,

    CONSTRAINT notification_type_check CHECK (
        type IN (
            'PLAN_REMINDER',
            'PLAN_START',
            'PLAN_OVERDUE',
            'ROUTINE_REMINDER',
            'ROUTINE_OVERDUE',
            'JOURNAL_REMINDER'
        )
    )
);

-- Notification preferences (SSOT: profile.user_preferences)
ALTER TABLE profile.user_preferences
    ADD COLUMN plan_notifications_enabled BOOLEAN DEFAULT TRUE,
    ADD COLUMN plan_reminder_minutes INTEGER DEFAULT 15,
    ADD COLUMN routine_notifications_enabled BOOLEAN DEFAULT TRUE,
    ADD COLUMN journal_notifications_enabled BOOLEAN DEFAULT TRUE;

ALTER TABLE profile.user_preferences
    ADD CONSTRAINT plan_reminder_minutes_check
        CHECK (plan_reminder_minutes IN (15, 30, 60));

-- Routine reminders
-- Entity Type: BaseEntity
CREATE TABLE routine.routine_reminders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    routine_id UUID NOT NULL REFERENCES routine.routines(id) ON DELETE CASCADE,
    reminder_time TIME NOT NULL,

    -- BaseEntity fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID,

    UNIQUE (routine_id, reminder_time)
);

CREATE INDEX idx_notifications_user_read ON notification.notifications(user_id, is_read, created_at DESC);
CREATE INDEX idx_notifications_user_created_at ON notification.notifications(user_id, created_at DESC);
CREATE INDEX idx_routine_reminders_routine ON routine.routine_reminders(routine_id);
