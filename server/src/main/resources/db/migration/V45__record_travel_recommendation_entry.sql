CREATE TABLE travel_recommendation_entries (
    session_id UUID NOT NULL,
    source VARCHAR(6) NOT NULL CHECK (source IN ('CITY', 'PERIOD')),
    kind VARCHAR(15) NOT NULL CHECK (kind IN ('EXPOSED', 'CLICKED', 'LOGIN_RESUMED')),
    signed_in BOOLEAN NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT statement_timestamp(),
    PRIMARY KEY (session_id, source, kind, signed_in),
    CHECK (kind <> 'LOGIN_RESUMED' OR signed_in)
);

ALTER TABLE travel_usage_cohorts ADD COLUMN recommendation_source VARCHAR(6)
    CHECK (recommendation_source IN ('CITY', 'PERIOD'));
