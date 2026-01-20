-- =====================================================
-- THAT MOMENT APP - DATABASE SCHEMA
-- =====================================================
-- Version: 2.0
-- Description: Complete database schema for That Moment App
-- BaseEntity Hierarchy:
--   - Immutable: sadece id + created_at
--   - BaseEntity: id + created_at + updated_at + created_by + updated_by
--   - SoftDeletableEntity: BaseEntity + deleted_at + deleted_by + delete_reason
--   - VersionedBaseEntity: BaseEntity + version (soft delete YOK)
--   - VersionedSoftDeletableEntity: SoftDeletableEntity + version
-- =====================================================

-- =====================================================
-- SCHEMA CREATION
-- =====================================================

CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS profile;
CREATE SCHEMA IF NOT EXISTS journal;
CREATE SCHEMA IF NOT EXISTS routine;
CREATE SCHEMA IF NOT EXISTS calendar;
CREATE SCHEMA IF NOT EXISTS gamification;
CREATE SCHEMA IF NOT EXISTS subscription;
CREATE SCHEMA IF NOT EXISTS notification;
CREATE SCHEMA IF NOT EXISTS sync;
CREATE SCHEMA IF NOT EXISTS analytics;

-- =====================================================
-- AUTH SCHEMA - Authentication & Authorization
-- =====================================================

-- Users (Core identity)
-- Entity Type: SoftDeletableEntity
CREATE TABLE auth.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255), -- Nullable for OAuth-only users
    auth_method VARCHAR(50) NOT NULL DEFAULT 'EMAIL', -- 'EMAIL', 'GOOGLE', 'APPLE'
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    verified_at TIMESTAMP,
    last_login_at TIMESTAMP,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP,

    -- SoftDeletableEntity fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP,
    deleted_by UUID,
    delete_reason VARCHAR(500),

    CONSTRAINT valid_email CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- OAuth providers (Google, Apple)
-- Entity Type: BaseEntity
CREATE TABLE auth.oauth_providers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    provider_name VARCHAR(50) NOT NULL, -- 'google', 'apple'
    provider_user_id VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    access_token TEXT,
    refresh_token TEXT,
    token_expires_at TIMESTAMP,
    profile_data JSONB,
    first_login_at TIMESTAMP DEFAULT NOW(),
    last_login_at TIMESTAMP,

    -- BaseEntity fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID,

    UNIQUE(provider_name, provider_user_id),
    UNIQUE(provider_name, user_id)
);

-- Sessions
-- Entity Type: BaseEntity
CREATE TABLE auth.sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    session_token VARCHAR(500) NOT NULL UNIQUE,
    device_id VARCHAR(255),
    device_name VARCHAR(100),
    platform VARCHAR(20), -- 'ios', 'android'
    ip_address INET,
    user_agent TEXT,
    auth_method VARCHAR(50),
    expires_at TIMESTAMP NOT NULL,
    last_activity_at TIMESTAMP DEFAULT NOW(),
    is_active BOOLEAN DEFAULT TRUE,
    revoked_at TIMESTAMP,
    revoked_reason VARCHAR(50), -- 'USER_LOGOUT', 'FORCE_LOGOUT', 'SECURITY', 'TOKEN_EXPIRED'

    -- BaseEntity fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID
);

-- Refresh tokens
-- Entity Type: BaseEntity
CREATE TABLE auth.refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    session_id UUID REFERENCES auth.sessions(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    device_id VARCHAR(255),
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_ip INET,
    is_active BOOLEAN DEFAULT TRUE,

    -- BaseEntity fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID
);

-- Device sessions (for device management)
-- Entity Type: BaseEntity
CREATE TABLE auth.device_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    device_id VARCHAR(255) NOT NULL,
    device_name VARCHAR(100),
    platform VARCHAR(20), -- 'ios', 'android'
    os_version VARCHAR(50),
    app_version VARCHAR(20),
    last_ip_address INET,
    first_seen_at TIMESTAMP DEFAULT NOW(),
    last_seen_at TIMESTAMP DEFAULT NOW(),
    is_active BOOLEAN DEFAULT TRUE,

    -- BaseEntity fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID,

    UNIQUE(user_id, device_id)
);

-- Login history
-- Entity Type: Immutable (audit log - no updates)
CREATE TABLE auth.login_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    login_at TIMESTAMP DEFAULT NOW(),
    auth_method VARCHAR(50),
    device_id VARCHAR(255),
    ip_address INET,
    user_agent TEXT,
    status VARCHAR(20) NOT NULL, -- 'success', 'failed', 'blocked'
    failure_reason VARCHAR(100),

    -- Immutable - only created_at
    created_at TIMESTAMP(6) NOT NULL DEFAULT now()
);

-- Email verifications
-- Entity Type: BaseEntity
CREATE TABLE auth.email_verifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    code VARCHAR(6) NOT NULL,
    purpose VARCHAR(20) NOT NULL, -- 'EMAIL_VERIFY', 'LOGIN_OTP', 'PASSWORD_RESET'
    attempt_count INTEGER NOT NULL DEFAULT 0,
    max_attempts INTEGER NOT NULL DEFAULT 3,
    expires_at TIMESTAMP NOT NULL,
    verified_at TIMESTAMP,

    -- BaseEntity fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID
);

-- Password reset tokens
-- Entity Type: BaseEntity
CREATE TABLE auth.password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,

    -- BaseEntity fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID
);


-- =====================================================
-- PROFILE SCHEMA - User Profile & Preferences
-- =====================================================

-- User profiles
-- Entity Type: SoftDeletableEntity
CREATE TABLE profile.user_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES auth.users(id) ON DELETE CASCADE,
    name VARCHAR(100),
    avatar_url VARCHAR(500),
    date_of_birth DATE,

    -- SoftDeletableEntity fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP,
    deleted_by UUID,
    delete_reason VARCHAR(500)
);

-- User preferences
-- Entity Type: SoftDeletableEntity
CREATE TABLE profile.user_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES auth.users(id) ON DELETE CASCADE,
    theme VARCHAR(20) DEFAULT 'light', -- 'light', 'dark', 'auto'
    language VARCHAR(10) DEFAULT 'tr', -- 'tr', 'en'
    timezone VARCHAR(50) DEFAULT 'Europe/Istanbul',
    week_start_day VARCHAR(10) DEFAULT 'monday', -- 'monday', 'sunday'
    time_format VARCHAR(5) DEFAULT '24h', -- '12h', '24h'
    notification_routines BOOLEAN DEFAULT TRUE,
    notification_achievements BOOLEAN DEFAULT TRUE,
    notification_streaks BOOLEAN DEFAULT TRUE,
    notification_daily_reminder BOOLEAN DEFAULT TRUE,
    daily_reminder_time TIME DEFAULT '09:00:00',
    journal_lock_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    journal_password_hash VARCHAR(255),

    -- SoftDeletableEntity fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP,
    deleted_by UUID,
    delete_reason VARCHAR(500)
);

-- =====================================================
-- SUBSCRIPTION SCHEMA - Plans & Subscriptions
-- =====================================================

-- Subscription plans
-- Entity Type: BaseEntity (admin managed, no soft delete)
CREATE TABLE subscription.plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE, -- 'FREE', 'NO_ADS', 'PREMIUM', 'PREMIUM_PLUS'
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    limits JSONB NOT NULL, -- Limit detayları
    price_monthly DECIMAL(10,2),
    price_yearly DECIMAL(10,2),
    apple_product_id_monthly VARCHAR(100),
    apple_product_id_yearly VARCHAR(100),
    google_product_id_monthly VARCHAR(100),
    google_product_id_yearly VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    sort_order INTEGER DEFAULT 0,

    -- BaseEntity fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID
);

-- User subscriptions
-- Entity Type: VersionedBaseEntity (optimistic locking, NO soft delete - status managed)
CREATE TABLE subscription.user_subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    plan_id UUID NOT NULL REFERENCES subscription.plans(id),
    status VARCHAR(20) NOT NULL DEFAULT 'active', -- 'active', 'cancelled', 'expired', 'grace_period'
    billing_period VARCHAR(20), -- 'monthly', 'yearly', NULL for free
    started_at TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancel_reason VARCHAR(255),
    auto_renew BOOLEAN DEFAULT TRUE,
    platform VARCHAR(20), -- 'apple', 'google', NULL for free
    original_transaction_id VARCHAR(255),

    -- VersionedBaseEntity fields (NO soft delete)
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0
);

-- Subscription receipts (IAP validation)
-- Entity Type: Immutable (financial record - no updates)
CREATE TABLE subscription.receipts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    subscription_id UUID REFERENCES subscription.user_subscriptions(id),
    platform VARCHAR(20) NOT NULL, -- 'apple', 'google'
    transaction_id VARCHAR(255) NOT NULL,
    original_transaction_id VARCHAR(255),
    product_id VARCHAR(100) NOT NULL,
    receipt_data TEXT,
    purchase_date TIMESTAMP,
    expires_date TIMESTAMP,
    is_trial BOOLEAN DEFAULT FALSE,
    is_sandbox BOOLEAN DEFAULT FALSE,
    validation_status VARCHAR(20), -- 'valid', 'invalid', 'pending'
    validation_response JSONB,

    -- Immutable - only created_at
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),

    UNIQUE(platform, transaction_id)
);

-- =====================================================
-- JOURNAL SCHEMA - Digital Bullet Journal
-- =====================================================

-- Journal tags
-- Entity Type: SoftDeletableEntity
CREATE TABLE journal.tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL,
    color VARCHAR(7) DEFAULT '#4ade80',
    usage_count INTEGER DEFAULT 0,

    -- SoftDeletableEntity fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP,
    deleted_by UUID,
    delete_reason VARCHAR(500),

    UNIQUE(user_id, name)
);

-- Journal entries
-- Entity Type: SoftDeletableEntity
CREATE TABLE journal.entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    local_id VARCHAR(100), -- Client-side ID for sync
    entry_date DATE NOT NULL,
    content TEXT,
    mood INTEGER CHECK (mood >= 1 AND mood <= 5), -- 1-5 emoji scale
    gratitude TEXT[], -- Array of gratitude items
    is_favorite BOOLEAN DEFAULT FALSE,
    word_count INTEGER DEFAULT 0,

    -- SoftDeletableEntity fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP,
    deleted_by UUID,
    delete_reason VARCHAR(500),

    UNIQUE(user_id, local_id)
);

-- Journal entry tags (many-to-many)
-- Entity Type: Immutable (junction table)
CREATE TABLE journal.entry_tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entry_id UUID NOT NULL REFERENCES journal.entries(id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES journal.tags(id) ON DELETE CASCADE,

    -- Immutable - only created_at
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),

    UNIQUE(entry_id, tag_id)
);

-- Journal media (Premium feature)
-- Entity Type: SoftDeletableEntity
CREATE TABLE journal.media (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entry_id UUID NOT NULL REFERENCES journal.entries(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    media_type VARCHAR(20) NOT NULL, -- 'photo', 'audio'
    s3_key VARCHAR(500) NOT NULL,
    file_name VARCHAR(255),
    file_size INTEGER, -- bytes
    duration_seconds INTEGER, -- for audio
    mime_type VARCHAR(100),

    -- SoftDeletableEntity fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP,
    deleted_by UUID,
    delete_reason VARCHAR(500)
);


-- =====================================================
-- ROUTINE SCHEMA - Habit Tracking
-- =====================================================

-- Routine definitions
-- Entity Type: SoftDeletableEntity
CREATE TABLE routine.routines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(1000),
    type VARCHAR(20) NOT NULL, -- 'CHECK', 'NUMERIC'
    target_value INTEGER,
    unit VARCHAR(50),
    start_date DATE,
    end_date DATE,
    is_active BOOLEAN DEFAULT TRUE,

    -- SoftDeletableEntity fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP,
    deleted_by UUID,
    delete_reason VARCHAR(500),
    CONSTRAINT routine_type_check CHECK (type IN ('CHECK', 'NUMERIC')),
    CONSTRAINT routine_dates_check CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date),
    CONSTRAINT routine_target_value_check CHECK (
        (type = 'CHECK' AND target_value IS NULL AND unit IS NULL)
        OR (type = 'NUMERIC' AND target_value IS NOT NULL AND target_value > 0 AND unit IS NOT NULL)
    )
);

-- Routine schedules
-- Entity Type: BaseEntity
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

-- Routine progress
-- Entity Type: BaseEntity
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

-- =====================================================
-- CALENDAR SCHEMA - Time Blocking
-- =====================================================

-- Time block categories
-- Entity Type: SoftDeletableEntity
CREATE TABLE calendar.categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL,
    color VARCHAR(7) NOT NULL,
    icon VARCHAR(50),
    is_default BOOLEAN DEFAULT FALSE,

    -- SoftDeletableEntity fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP,
    deleted_by UUID,
    delete_reason VARCHAR(500),

    UNIQUE(user_id, name)
);

-- Time blocks
-- Entity Type: SoftDeletableEntity
CREATE TABLE calendar.time_blocks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    local_id VARCHAR(100),
    category_id UUID REFERENCES calendar.categories(id) ON DELETE SET NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    block_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_all_day BOOLEAN DEFAULT FALSE,
    recurrence VARCHAR(20) DEFAULT 'none', -- 'none', 'daily', 'weekly', 'monthly'
    recurrence_end_date DATE,
    location VARCHAR(255),
    reminder_minutes INTEGER[], -- Array: [15, 60] = 15 min and 1 hour before
    is_completed BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMP,

    -- SoftDeletableEntity fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP,
    deleted_by UUID,
    delete_reason VARCHAR(500),

    UNIQUE(user_id, local_id),
    CONSTRAINT valid_time_range CHECK (start_time < end_time OR is_all_day = TRUE)
);

-- =====================================================
-- GAMIFICATION SCHEMA - Coins, XP, Achievements
-- =====================================================

-- User stats
-- Entity Type: VersionedBaseEntity (optimistic locking for coin updates, NO soft delete)
CREATE TABLE gamification.user_stats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES auth.users(id) ON DELETE CASCADE,
    coins INTEGER NOT NULL DEFAULT 100, -- Starting bonus
    xp INTEGER NOT NULL DEFAULT 0,
    level INTEGER NOT NULL DEFAULT 1,
    xp_to_next_level INTEGER NOT NULL DEFAULT 100,
    total_journal_entries INTEGER DEFAULT 0,
    total_routines_completed INTEGER DEFAULT 0,
    last_daily_bonus_at DATE,
    streak_saves_used_this_month INTEGER DEFAULT 0,
    exports_used_this_month INTEGER DEFAULT 0,

    -- VersionedBaseEntity fields (NO soft delete)
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0
);

-- Coin transactions (ledger)
-- Entity Type: Immutable (financial ledger - source of truth)
CREATE TABLE gamification.coin_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    amount INTEGER NOT NULL, -- positive for credit, negative for debit
    balance_after INTEGER NOT NULL,
    transaction_type VARCHAR(30) NOT NULL, -- 'DAILY_BONUS', 'ACHIEVEMENT', 'PURCHASE', 'STREAK_REPAIR', 'ADMIN_CREDIT', etc.
    reference_type VARCHAR(30), -- 'achievement', 'store_item', 'streak', etc.
    reference_id UUID,
    description VARCHAR(255),
    idempotency_key VARCHAR(100) UNIQUE,

    -- Immutable - only created_at
    created_at TIMESTAMP(6) NOT NULL DEFAULT now()
);

-- Streaks
-- Entity Type: VersionedSoftDeletableEntity (optimistic locking + soft delete)
CREATE TABLE gamification.streaks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    streak_type VARCHAR(50) NOT NULL, -- 'daily_journal', 'daily_login', 'exercise', 'water', etc.
    name VARCHAR(100) NOT NULL,
    current_streak INTEGER NOT NULL DEFAULT 0,
    longest_streak INTEGER NOT NULL DEFAULT 0,
    last_completed_date DATE,
    streak_start_date DATE,
    is_active BOOLEAN DEFAULT TRUE,
    icon VARCHAR(50),
    color VARCHAR(7),

    -- VersionedSoftDeletableEntity fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP,
    deleted_by UUID,
    delete_reason VARCHAR(500),
    version BIGINT NOT NULL DEFAULT 0,

    UNIQUE(user_id, streak_type)
);

-- Streak history
-- Entity Type: Immutable (event log)
CREATE TABLE gamification.streak_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    streak_type VARCHAR(50) NOT NULL,
    event_type VARCHAR(20) NOT NULL, -- 'BREAK', 'REPAIR', 'MILESTONE'
    streak_before INTEGER NOT NULL,
    streak_after INTEGER NOT NULL,
    coins_spent INTEGER, -- for REPAIR events
    milestone_days INTEGER, -- for MILESTONE events
    event_date DATE NOT NULL,

    -- Immutable - only created_at
    created_at TIMESTAMP(6) NOT NULL DEFAULT now()
);

-- Achievement definitions
-- Entity Type: BaseEntity (admin managed)
CREATE TABLE gamification.achievements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    icon VARCHAR(50),
    achievement_type VARCHAR(50) NOT NULL, -- 'streak', 'consistency', 'milestone', 'special'
    requirement_type VARCHAR(50) NOT NULL, -- 'streak_days', 'total_entries', 'perfect_week', 'level_reached'
    requirement_value INTEGER NOT NULL,
    requirement_category VARCHAR(50), -- For streak-based: 'daily_journal', 'exercise', etc.
    reward_coins INTEGER NOT NULL DEFAULT 0,
    reward_xp INTEGER NOT NULL DEFAULT 0,
    reward_unlocks TEXT[], -- Premium themes, stickers, etc.
    rarity VARCHAR(20) NOT NULL DEFAULT 'common', -- 'common', 'rare', 'epic', 'legendary'
    sort_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,

    -- BaseEntity fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID
);

-- User achievements (unlocked)
-- Entity Type: Immutable (event record)
CREATE TABLE gamification.user_achievements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    achievement_id UUID NOT NULL REFERENCES gamification.achievements(id),
    unlocked_at TIMESTAMP NOT NULL DEFAULT NOW(),
    notified BOOLEAN DEFAULT FALSE,

    -- Immutable - only created_at
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),

    UNIQUE(user_id, achievement_id)
);

-- Store items
-- Entity Type: BaseEntity (admin managed)
CREATE TABLE gamification.store_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    item_type VARCHAR(50) NOT NULL, -- 'theme', 'sticker_pack', 'journal_template', 'calendar_style'
    name VARCHAR(100) NOT NULL,
    description TEXT,
    preview_urls TEXT[],
    price_coins INTEGER NOT NULL,
    price_type VARCHAR(20) DEFAULT 'coins', -- 'coins', 'premium_only'
    unlock_requirement_type VARCHAR(50), -- 'level', 'achievement', 'streak', NULL
    unlock_requirement_value VARCHAR(100),
    rarity VARCHAR(20) DEFAULT 'common',
    is_active BOOLEAN DEFAULT TRUE,
    sort_order INTEGER DEFAULT 0,

    -- BaseEntity fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID
);

-- User purchases
-- Entity Type: Immutable (transaction record)
CREATE TABLE gamification.user_purchases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    item_id UUID NOT NULL REFERENCES gamification.store_items(id),
    purchased_at TIMESTAMP NOT NULL DEFAULT NOW(),
    price_paid INTEGER NOT NULL,

    -- Immutable - only created_at
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),

    UNIQUE(user_id, item_id)
);


-- =====================================================
-- NOTIFICATION SCHEMA - Push Notifications
-- =====================================================

-- User devices (FCM tokens)
-- Entity Type: BaseEntity
CREATE TABLE notification.user_devices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    device_id VARCHAR(255) NOT NULL,
    fcm_token TEXT NOT NULL,
    platform VARCHAR(20) NOT NULL, -- 'ios', 'android'
    device_name VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,

    -- BaseEntity fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID,

    UNIQUE(user_id, device_id)
);

-- Notification logs
-- Entity Type: Immutable (delivery log)
CREATE TABLE notification.logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    notification_type VARCHAR(50) NOT NULL, -- 'routine_reminder', 'streak_warning', 'achievement', 'daily_bonus'
    title VARCHAR(200) NOT NULL,
    body TEXT,
    data JSONB,
    sent_at TIMESTAMP NOT NULL DEFAULT NOW(),
    read_at TIMESTAMP,
    clicked_at TIMESTAMP,
    fcm_message_id VARCHAR(255),
    status VARCHAR(20) DEFAULT 'sent', -- 'sent', 'delivered', 'failed'
    error_message TEXT,

    -- Immutable - only created_at
    created_at TIMESTAMP(6) NOT NULL DEFAULT now()
);

-- =====================================================
-- SYNC SCHEMA - Offline-First Sync
-- =====================================================

-- Sync queue (for conflict resolution)
-- Entity Type: Immutable (sync event)
CREATE TABLE sync.queue (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    entity_type VARCHAR(50) NOT NULL, -- 'journal_entry', 'routine_entry', 'time_block'
    entity_id UUID NOT NULL,
    local_id VARCHAR(100),
    action VARCHAR(20) NOT NULL, -- 'create', 'update', 'delete'
    payload JSONB NOT NULL,
    client_timestamp TIMESTAMP NOT NULL,
    server_timestamp TIMESTAMP DEFAULT NOW(),
    status VARCHAR(20) DEFAULT 'pending', -- 'pending', 'synced', 'conflict'
    conflict_data JSONB,
    resolved_at TIMESTAMP,

    -- Immutable - only created_at
    created_at TIMESTAMP(6) NOT NULL DEFAULT now()
);

-- Sync metadata (last sync timestamps)
-- Entity Type: BaseEntity
CREATE TABLE sync.metadata (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    device_id VARCHAR(255) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    last_sync_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_server_version BIGINT DEFAULT 0,

    -- BaseEntity fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID,

    UNIQUE(user_id, device_id, entity_type)
);

-- =====================================================
-- ANALYTICS SCHEMA - Basic Analytics
-- =====================================================

-- Weekly reviews
-- Entity Type: SoftDeletableEntity
CREATE TABLE analytics.weekly_reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    week_start DATE NOT NULL, -- Monday of the week
    wins TEXT[],
    challenges TEXT[],
    learnings TEXT[],
    goals_for_next_week TEXT[],
    routine_summary JSONB, -- {routine_id: completion_rate}
    mood_trend INTEGER[], -- Array of daily moods
    average_mood DECIMAL(3,2),
    total_journal_entries INTEGER DEFAULT 0,
    total_routines_completed INTEGER DEFAULT 0,

    -- SoftDeletableEntity fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP,
    deleted_by UUID,
    delete_reason VARCHAR(500),

    UNIQUE(user_id, week_start)
);

-- Daily usage stats (for limit checking)
-- Entity Type: BaseEntity
CREATE TABLE analytics.daily_usage (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    usage_date DATE NOT NULL,
    journal_entries_count INTEGER DEFAULT 0,
    time_blocks_count INTEGER DEFAULT 0,
    photos_uploaded INTEGER DEFAULT 0,
    audio_uploaded INTEGER DEFAULT 0,
    storage_used_bytes BIGINT DEFAULT 0,

    -- BaseEntity fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID,

    UNIQUE(user_id, usage_date)
);

-- =====================================================
-- INDEXES
-- =====================================================

-- Auth indexes
CREATE INDEX idx_users_email ON auth.users(email) WHERE deleted_at IS NULL;
CREATE INDEX idx_users_status ON auth.users(is_active, is_verified) WHERE deleted_at IS NULL;
CREATE INDEX idx_oauth_providers_user ON auth.oauth_providers(user_id);
CREATE INDEX idx_sessions_user ON auth.sessions(user_id) WHERE is_active = TRUE;
CREATE INDEX idx_sessions_token ON auth.sessions(session_token) WHERE is_active = TRUE;
CREATE INDEX idx_sessions_expiry ON auth.sessions(expires_at) WHERE is_active = TRUE;
CREATE INDEX idx_refresh_tokens_user ON auth.refresh_tokens(user_id) WHERE is_active = TRUE;
CREATE INDEX idx_device_sessions_user ON auth.device_sessions(user_id) WHERE is_active = TRUE;
CREATE INDEX idx_login_history_user ON auth.login_history(user_id, created_at DESC);
CREATE INDEX idx_email_verifications_user ON auth.email_verifications(user_id, purpose) WHERE verified_at IS NULL;

-- Profile indexes
CREATE INDEX idx_user_profiles_user ON profile.user_profiles(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_user_preferences_user ON profile.user_preferences(user_id) WHERE deleted_at IS NULL;

-- Subscription indexes
CREATE INDEX idx_user_subscriptions_user ON subscription.user_subscriptions(user_id);
CREATE INDEX idx_user_subscriptions_status ON subscription.user_subscriptions(status, expires_at);
CREATE INDEX idx_receipts_user ON subscription.receipts(user_id);
CREATE INDEX idx_receipts_transaction ON subscription.receipts(platform, original_transaction_id);

-- Journal indexes
CREATE INDEX idx_journal_tags_user ON journal.tags(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_journal_entries_user ON journal.entries(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_journal_entries_date ON journal.entries(user_id, entry_date DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_journal_entries_favorite ON journal.entries(user_id, is_favorite) WHERE deleted_at IS NULL AND is_favorite = TRUE;
CREATE INDEX idx_journal_entries_local ON journal.entries(user_id, local_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_journal_entry_tags_entry ON journal.entry_tags(entry_id);
CREATE INDEX idx_journal_entry_tags_tag ON journal.entry_tags(tag_id);
CREATE INDEX idx_journal_media_entry ON journal.media(entry_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_journal_media_user ON journal.media(user_id) WHERE deleted_at IS NULL;

-- Routine indexes
CREATE INDEX idx_routines_user_active ON routine.routines(user_id, is_active);
CREATE INDEX idx_routines_user_deleted ON routine.routines(user_id, deleted_at);
CREATE INDEX idx_routine_schedules_routine ON routine.routine_schedules(routine_id);
CREATE INDEX idx_routine_progress_user_date ON routine.routine_progress(user_id, progress_date);
CREATE INDEX idx_routine_progress_routine_date ON routine.routine_progress(routine_id, progress_date);

-- Calendar indexes
CREATE INDEX idx_calendar_categories_user ON calendar.categories(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_time_blocks_user ON calendar.time_blocks(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_time_blocks_date ON calendar.time_blocks(user_id, block_date) WHERE deleted_at IS NULL;
CREATE INDEX idx_time_blocks_local ON calendar.time_blocks(user_id, local_id) WHERE deleted_at IS NULL;

-- Gamification indexes
CREATE INDEX idx_user_stats_user ON gamification.user_stats(user_id);
CREATE INDEX idx_coin_transactions_user ON gamification.coin_transactions(user_id, created_at DESC);
CREATE INDEX idx_coin_transactions_idempotency ON gamification.coin_transactions(idempotency_key) WHERE idempotency_key IS NOT NULL;
CREATE INDEX idx_streaks_user ON gamification.streaks(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_streaks_type ON gamification.streaks(user_id, streak_type) WHERE deleted_at IS NULL;
CREATE INDEX idx_streak_history_user ON gamification.streak_history(user_id, created_at DESC);
CREATE INDEX idx_user_achievements_user ON gamification.user_achievements(user_id);
CREATE INDEX idx_user_purchases_user ON gamification.user_purchases(user_id);

-- Notification indexes
CREATE INDEX idx_user_devices_user ON notification.user_devices(user_id) WHERE is_active = TRUE;
CREATE INDEX idx_notification_logs_user ON notification.logs(user_id, created_at DESC);

-- Sync indexes
CREATE INDEX idx_sync_queue_user ON sync.queue(user_id, status);
CREATE INDEX idx_sync_queue_entity ON sync.queue(entity_type, entity_id);
CREATE INDEX idx_sync_metadata_user ON sync.metadata(user_id, device_id);

-- Analytics indexes
CREATE INDEX idx_weekly_reviews_user ON analytics.weekly_reviews(user_id, week_start DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_daily_usage_user ON analytics.daily_usage(user_id, usage_date DESC);

-- =====================================================
-- COMMENTS
-- =====================================================

COMMENT ON SCHEMA auth IS 'Authentication and authorization tables';
COMMENT ON SCHEMA profile IS 'User profile and preferences';
COMMENT ON SCHEMA subscription IS 'Subscription plans and user subscriptions';
COMMENT ON SCHEMA journal IS 'Digital bullet journal entries and media';
COMMENT ON SCHEMA routine IS 'Habit tracking routines and entries';
COMMENT ON SCHEMA calendar IS 'Time blocking and scheduling';
COMMENT ON SCHEMA gamification IS 'Coins, XP, achievements, and store';
COMMENT ON SCHEMA notification IS 'Push notification management';
COMMENT ON SCHEMA sync IS 'Offline-first sync queue and metadata';
COMMENT ON SCHEMA analytics IS 'Usage analytics and weekly reviews';

-- Entity type comments
COMMENT ON TABLE auth.users IS 'Entity: SoftDeletableEntity - Core user identity';
COMMENT ON TABLE auth.oauth_providers IS 'Entity: BaseEntity - OAuth provider connections';
COMMENT ON TABLE auth.sessions IS 'Entity: BaseEntity - Active user sessions';
COMMENT ON TABLE auth.refresh_tokens IS 'Entity: BaseEntity - JWT refresh tokens';
COMMENT ON TABLE auth.device_sessions IS 'Entity: BaseEntity - Device management';
COMMENT ON TABLE auth.login_history IS 'Entity: Immutable - Login audit log';
COMMENT ON TABLE auth.email_verifications IS 'Entity: BaseEntity - Email verification codes';
COMMENT ON TABLE auth.password_reset_tokens IS 'Entity: BaseEntity - Password reset tokens';

COMMENT ON TABLE profile.user_profiles IS 'Entity: SoftDeletableEntity - User profile data';
COMMENT ON TABLE profile.user_preferences IS 'Entity: SoftDeletableEntity - User preferences';

COMMENT ON TABLE subscription.plans IS 'Entity: BaseEntity - Subscription plan definitions';
COMMENT ON TABLE subscription.user_subscriptions IS 'Entity: VersionedBaseEntity - User subscriptions with optimistic locking';
COMMENT ON TABLE subscription.receipts IS 'Entity: Immutable - IAP receipt records';

COMMENT ON TABLE journal.tags IS 'Entity: SoftDeletableEntity - User-created tags';
COMMENT ON TABLE journal.entries IS 'Entity: SoftDeletableEntity - Journal entries';
COMMENT ON TABLE journal.entry_tags IS 'Entity: Immutable - Junction table for entry-tag relationship';
COMMENT ON TABLE journal.media IS 'Entity: SoftDeletableEntity - Media attachments';

COMMENT ON TABLE routine.routines IS 'Entity: SoftDeletableEntity - Habit definitions';

COMMENT ON TABLE calendar.categories IS 'Entity: SoftDeletableEntity - Time block categories';
COMMENT ON TABLE calendar.time_blocks IS 'Entity: SoftDeletableEntity - Scheduled time blocks';

COMMENT ON TABLE gamification.user_stats IS 'Entity: VersionedBaseEntity - User coins/XP with optimistic locking';
COMMENT ON TABLE gamification.coin_transactions IS 'Entity: Immutable - Coin transaction ledger';
COMMENT ON TABLE gamification.streaks IS 'Entity: VersionedSoftDeletableEntity - User streaks with optimistic locking';
COMMENT ON TABLE gamification.streak_history IS 'Entity: Immutable - Streak event history';
COMMENT ON TABLE gamification.achievements IS 'Entity: BaseEntity - Achievement definitions';
COMMENT ON TABLE gamification.user_achievements IS 'Entity: Immutable - Unlocked achievements';
COMMENT ON TABLE gamification.store_items IS 'Entity: BaseEntity - Store item definitions';
COMMENT ON TABLE gamification.user_purchases IS 'Entity: Immutable - User purchase records';

COMMENT ON TABLE notification.user_devices IS 'Entity: BaseEntity - FCM device tokens';
COMMENT ON TABLE notification.logs IS 'Entity: Immutable - Notification delivery log';

COMMENT ON TABLE sync.queue IS 'Entity: Immutable - Offline sync queue';
COMMENT ON TABLE sync.metadata IS 'Entity: BaseEntity - Sync state tracking';

COMMENT ON TABLE analytics.weekly_reviews IS 'Entity: SoftDeletableEntity - Weekly review entries';
COMMENT ON TABLE analytics.daily_usage IS 'Entity: BaseEntity - Daily usage metrics';
