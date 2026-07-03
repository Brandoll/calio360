-- ============================================================
-- S-01 Identity Service - Schema
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
    id             BIGSERIAL PRIMARY KEY,
    email          VARCHAR(255) NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    first_name     VARCHAR(100) NOT NULL,
    last_name      VARCHAR(100) NOT NULL,
    birth_date     DATE,
    gender         VARCHAR(10) CHECK (gender IN ('MALE','FEMALE','OTHER')),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ,
    active         BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS biometric_records (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    weight_kg    DECIMAL(5,2),
    height_cm    DECIMAL(5,2),
    body_fat_pct DECIMAL(4,1),
    recorded_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_biometric_user_date
    ON biometric_records(user_id, recorded_at DESC);

CREATE TABLE IF NOT EXISTS user_goals (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    goal_type        VARCHAR(30) CHECK (goal_type IN ('LOSE_WEIGHT','GAIN_MUSCLE','MAINTAIN','EAT_HEALTHY')),
    target_weight_kg DECIMAL(5,2),
    daily_calories   INT,
    protein_grams    DECIMAL(5,2),
    carbs_grams      DECIMAL(5,2),
    fat_grams        DECIMAL(5,2),
    activity_level   VARCHAR(20) CHECK (activity_level IN ('SEDENTARY','LIGHT','MODERATE','ACTIVE','VERY_ACTIVE')),
    start_date       DATE NOT NULL DEFAULT CURRENT_DATE,
    end_date         DATE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_goals_user_active
    ON user_goals(user_id, end_date);

CREATE TABLE IF NOT EXISTS user_settings (
    id                    BIGSERIAL PRIMARY KEY,
    user_id               BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    unit_system           VARCHAR(10) NOT NULL DEFAULT 'METRIC'
                              CHECK (unit_system IN ('METRIC','IMPERIAL')),
    language              VARCHAR(10) NOT NULL DEFAULT 'es-PE',
    notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token      VARCHAR(512) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    revoked    BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_refresh_token ON refresh_tokens(token);
