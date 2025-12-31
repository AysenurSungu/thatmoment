-- =====================================================
-- PLAN MODULE TABLES
-- =====================================================

CREATE SCHEMA IF NOT EXISTS plan;

CREATE TABLE plan.plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(1000),
    plan_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    color VARCHAR(20),

    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP,
    deleted_by UUID,
    delete_reason VARCHAR(500),

    CONSTRAINT plans_time_range CHECK (end_time > start_time)
);

CREATE INDEX IF NOT EXISTS idx_plans_user_date
    ON plan.plans (user_id, plan_date)
    WHERE deleted_at IS NULL;
