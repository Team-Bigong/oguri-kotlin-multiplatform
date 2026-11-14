-- Only confirmed product actions; no user-authored text or external place data.
-- Nullable references preserve the original observation denominator after removal.
CREATE TABLE travel_usage_cohorts (
    id UUID PRIMARY KEY,
    plan_key UUID NOT NULL,
    travel_plan_id UUID REFERENCES travel_plans(id) ON DELETE SET NULL,
    member_id VARCHAR(100) REFERENCES members(id) ON DELETE SET NULL,
    entry_kind VARCHAR(12) NOT NULL CHECK (entry_kind IN ('CREATED', 'JOINED', 'EXISTING')),
    entry_source VARCHAR(14) NOT NULL CHECK (entry_source IN ('DIRECT', 'RECOMMENDATION')),
    started_at TIMESTAMPTZ NOT NULL DEFAULT statement_timestamp(),
    days_until_start INTEGER,
    initial_end_at TIMESTAMPTZ NOT NULL,
    UNIQUE (plan_key, member_id),
    CHECK ((entry_kind = 'EXISTING') = (days_until_start IS NULL))
);

CREATE INDEX travel_usage_plan_idx ON travel_usage_cohorts (travel_plan_id);
CREATE INDEX travel_usage_member_idx ON travel_usage_cohorts (member_id, started_at);

CREATE TABLE travel_usage_edits (
    cohort_id UUID NOT NULL REFERENCES travel_usage_cohorts(id),
    plan_key UUID NOT NULL,
    revision BIGINT NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT statement_timestamp(),
    kind VARCHAR(24) NOT NULL CHECK (kind IN (
        'VISIT_ADDED', 'VISIT_EDITED', 'VISIT_REMOVED', 'DAY_REORDERED', 'VISIT_UNDONE', 'TRIP_UPDATED'
    )),
    has_dated_visit BOOLEAN NOT NULL,
    after_participation BOOLEAN NOT NULL,
    PRIMARY KEY (plan_key, revision)
);

CREATE INDEX travel_usage_edit_cohort_idx ON travel_usage_edits (cohort_id, occurred_at);
