-- Preserve installations that already provisioned this member field outside Flyway.
ALTER TABLE members
    ADD COLUMN IF NOT EXISTS onboarding_completed BOOLEAN NOT NULL DEFAULT FALSE;
