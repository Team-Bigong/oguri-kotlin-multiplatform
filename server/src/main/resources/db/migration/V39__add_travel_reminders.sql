CREATE TABLE travel_reminder_devices (
    id UUID PRIMARY KEY,
    member_id VARCHAR(100) NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    revocation_hash VARCHAR(64) NOT NULL,
    push_token VARCHAR(4096),
    revoked_at TIMESTAMPTZ
);

CREATE UNIQUE INDEX travel_reminder_active_token_idx
    ON travel_reminder_devices (md5(push_token)) WHERE revoked_at IS NULL;

CREATE TABLE travel_reminders (
    id UUID PRIMARY KEY,
    travel_plan_id UUID NOT NULL REFERENCES travel_plans(id) ON DELETE CASCADE,
    member_id VARCHAR(100) NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    device_id UUID NOT NULL REFERENCES travel_reminder_devices(id) ON DELETE CASCADE,
    scheduled_at TIMESTAMPTZ NOT NULL,
    time_zone VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'HANDOFF_STARTED', 'SENT', 'FAILED', 'CANCELLED', 'EXPIRED')),
    handoff_started_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT statement_timestamp()
);

CREATE UNIQUE INDEX travel_reminder_current_idx ON travel_reminders (travel_plan_id, member_id)
    WHERE status IN ('PENDING', 'HANDOFF_STARTED', 'SENT', 'FAILED', 'EXPIRED');

CREATE INDEX travel_reminder_due_idx ON travel_reminders (scheduled_at) WHERE status = 'PENDING';
CREATE INDEX travel_reminder_device_idx ON travel_reminders (device_id);
