ALTER TABLE users
    ADD COLUMN failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN lock_until TIMESTAMP NULL,
    ADD COLUMN last_login_at TIMESTAMP NULL,
    ADD COLUMN last_login_ip VARCHAR(64) NULL,
    ADD COLUMN last_login_user_agent TEXT NULL;
