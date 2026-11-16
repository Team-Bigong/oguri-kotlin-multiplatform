ALTER TABLE travel_usage_activity DROP CONSTRAINT travel_usage_activity_kind_check;
ALTER TABLE travel_usage_activity ADD CONSTRAINT travel_usage_activity_kind_check CHECK (kind IN (
    'PLAN_DISPLAYED', 'OVERVIEW_VIEWED', 'DAY_SELECTED', 'PIN_SELECTED', 'CHANGES_VIEWED', 'DIRECTIONS_OPENED',
    'NEXT_HOLIDAY_OPENED', 'CHANGE_DAY_OPENED'
));

CREATE INDEX travel_usage_next_holiday_idx ON travel_usage_activity (cohort_id, occurred_at)
    WHERE kind = 'NEXT_HOLIDAY_OPENED';

ALTER TABLE travel_usage_cohorts ADD COLUMN first_reminder_opt_out_at TIMESTAMPTZ;
