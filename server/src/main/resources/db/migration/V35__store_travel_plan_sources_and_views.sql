ALTER TABLE travel_plans
    ADD COLUMN source_destination_id INTEGER,
    ADD COLUMN source_start_date DATE,
    ADD COLUMN source_end_date DATE,
    ADD CONSTRAINT travel_plan_source_dates_check CHECK (
        (source_start_date IS NULL AND source_end_date IS NULL)
        OR (source_start_date IS NOT NULL AND source_end_date IS NOT NULL AND source_start_date <= source_end_date)
    );

CREATE TABLE travel_plan_views (
    member_id VARCHAR(100) NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    travel_plan_id UUID NOT NULL REFERENCES travel_plans(id) ON DELETE CASCADE,
    day_number INTEGER NOT NULL CHECK (day_number >= 0),
    opened_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (member_id, travel_plan_id)
);
