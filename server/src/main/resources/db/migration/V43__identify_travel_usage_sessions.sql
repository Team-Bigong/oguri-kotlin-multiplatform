ALTER TABLE travel_usage_cohorts ADD COLUMN start_session_id UUID;
ALTER TABLE travel_usage_edits ADD COLUMN session_id UUID;

CREATE TABLE travel_usage_activity (
    cohort_id UUID NOT NULL REFERENCES travel_usage_cohorts(id),
    session_id UUID NOT NULL,
    kind VARCHAR(24) NOT NULL CHECK (kind IN (
        'PLAN_DISPLAYED', 'OVERVIEW_VIEWED', 'DAY_SELECTED', 'PIN_SELECTED', 'CHANGES_VIEWED', 'DIRECTIONS_OPENED'
    )),
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT statement_timestamp(),
    PRIMARY KEY (cohort_id, session_id, kind)
);
