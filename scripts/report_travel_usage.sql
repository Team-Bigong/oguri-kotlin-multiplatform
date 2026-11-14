-- Read-only base facts, grouped without member/plan identifiers.
-- These are NOT retention rates: session-qualified activity is collected separately.
WITH plan_edits AS (
    SELECT plan_key,
           min(occurred_at) FILTER (WHERE has_dated_visit) AS first_dated_save_at,
           min(occurred_at) FILTER (WHERE after_participation) AS first_collaborative_save_at
    FROM travel_usage_edits
    GROUP BY plan_key
), observed_activity AS (
    SELECT cohort_id,
           bool_or(kind = 'CHANGES_VIEWED') AS viewed_changes,
           bool_or(kind = 'CHANGE_DAY_OPENED') AS opened_changed_day,
           bool_or(kind = 'VISIT_SAVE_FAILED') AS visit_save_failed,
           bool_or(kind = 'VISIT_SAVE_CONFLICT') AS visit_save_conflict,
           bool_or(kind = 'VISIT_CONFLICT_RECOVERED') AS visit_conflict_recovered
    FROM travel_usage_activity
    WHERE kind IN ('CHANGES_VIEWED', 'CHANGE_DAY_OPENED', 'VISIT_SAVE_FAILED', 'VISIT_SAVE_CONFLICT', 'VISIT_CONFLICT_RECOVERED')
    GROUP BY cohort_id
)
SELECT c.entry_source,
       count(*) FILTER (WHERE c.entry_kind = 'CREATED') AS created_plans,
       count(*) FILTER (WHERE c.entry_kind = 'CREATED' AND e.first_dated_save_at IS NOT NULL) AS activated_plans,
       count(*) FILTER (WHERE c.entry_kind = 'CREATED' AND e.first_collaborative_save_at IS NOT NULL) AS collaborative_plans,
       count(*) FILTER (WHERE c.entry_kind = 'JOINED') AS joined_memberships,
       count(*) FILTER (WHERE a.viewed_changes OR a.opened_changed_day) AS changes_viewed_memberships,
       count(*) FILTER (WHERE a.opened_changed_day) AS changed_day_opened_memberships,
       count(*) FILTER (WHERE a.visit_save_failed) AS visit_save_failed_memberships,
       count(*) FILTER (WHERE a.visit_save_conflict OR a.visit_conflict_recovered) AS visit_save_conflict_memberships,
       count(*) FILTER (WHERE a.visit_conflict_recovered) AS visit_conflict_recovered_memberships,
       count(*) FILTER (WHERE c.first_reminder_opt_out_at IS NOT NULL) AS reminder_opt_out_memberships,
       count(*) FILTER (WHERE c.days_until_start >= 8 AND c.started_at + interval '7 days' <= statement_timestamp()) AS eligible_matured_memberships,
       count(*) FILTER (WHERE c.days_until_start >= 8 AND c.started_at + interval '7 days' > statement_timestamp()) AS pending_memberships,
       count(*) FILTER (WHERE c.days_until_start BETWEEN 1 AND 7) AS imminent_memberships,
       count(*) FILTER (WHERE c.days_until_start <= 0 AND c.initial_end_at > c.started_at) AS in_progress_memberships,
       count(*) FILTER (WHERE c.initial_end_at <= c.started_at AND c.entry_kind <> 'EXISTING') AS past_memberships,
       count(*) FILTER (WHERE c.entry_kind = 'EXISTING') AS unknown_start_memberships
FROM travel_usage_cohorts c
LEFT JOIN plan_edits e ON e.plan_key = c.plan_key
LEFT JOIN observed_activity a ON a.cohort_id = c.id
GROUP BY c.entry_source
ORDER BY c.entry_source;
