CREATE TABLE travel_plan_members (
    travel_plan_id UUID NOT NULL REFERENCES travel_plans(id) ON DELETE CASCADE,
    member_id VARCHAR(100) NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT statement_timestamp(),
    PRIMARY KEY (travel_plan_id, member_id)
);

CREATE INDEX travel_plan_members_member_idx ON travel_plan_members (member_id, travel_plan_id);

CREATE TABLE travel_plan_invites (
    token_hash VARCHAR(64) PRIMARY KEY,
    travel_plan_id UUID REFERENCES travel_plans(id) ON DELETE SET NULL,
    issued_by VARCHAR(100) REFERENCES members(id) ON DELETE SET NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ
);

CREATE INDEX travel_plan_invites_trip_idx ON travel_plan_invites (travel_plan_id);

CREATE TABLE travel_plan_member_commands (
    travel_plan_id UUID NOT NULL REFERENCES travel_plans(id) ON DELETE CASCADE,
    member_id VARCHAR(100) NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    request_id UUID NOT NULL,
    fingerprint VARCHAR(64) NOT NULL,
    PRIMARY KEY (travel_plan_id, member_id, request_id)
);

ALTER TABLE travel_plans ADD COLUMN structure_revision BIGINT NOT NULL DEFAULT 0;

CREATE TABLE travel_plan_changes (
    travel_plan_id UUID NOT NULL REFERENCES travel_plans(id) ON DELETE CASCADE,
    revision BIGINT NOT NULL,
    actor_id VARCHAR(100) REFERENCES members(id) ON DELETE SET NULL,
    day_number INTEGER,
    kind VARCHAR(24) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT statement_timestamp(),
    PRIMARY KEY (travel_plan_id, revision)
);

ALTER TABLE travel_plan_views ADD COLUMN seen_revision BIGINT NOT NULL DEFAULT 0;
