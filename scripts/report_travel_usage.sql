-- Read-only base facts, grouped without member/plan identifiers.
-- These are NOT retention rates: session-qualified activity is collected separately.
WITH plan_edits AS (
    SELECT plan_key,
           min(occurred_at) FILTER (WHERE has_dated_visit) AS first_dated_save_at,
           min(occurred_at) FILTER (WHERE after_participation) AS first_collaborative_save_at
    FROM travel_usage_edits
    GROUP BY plan_key
)
SELECT c.entry_source,
       count(*) FILTER (WHERE c.entry_kind = 'CREATED') AS created_plans,
       count(*) FILTER (WHERE c.entry_kind = 'CREATED' AND e.first_dated_save_at IS NOT NULL) AS activated_plans,
       count(*) FILTER (WHERE c.entry_kind = 'CREATED' AND e.first_collaborative_save_at IS NOT NULL) AS collaborative_plans,
       count(*) FILTER (WHERE c.entry_kind = 'JOINED') AS joined_memberships,
       count(*) FILTER (WHERE c.days_until_start >= 8 AND c.started_at + interval '7 days' <= statement_timestamp()) AS eligible_matured_memberships,
       count(*) FILTER (WHERE c.days_until_start >= 8 AND c.started_at + interval '7 days' > statement_timestamp()) AS pending_memberships,
       count(*) FILTER (WHERE c.days_until_start BETWEEN 1 AND 7) AS imminent_memberships,
       count(*) FILTER (WHERE c.days_until_start <= 0 AND c.initial_end_at > c.started_at) AS in_progress_memberships,
       count(*) FILTER (WHERE c.initial_end_at <= c.started_at AND c.entry_kind <> 'EXISTING') AS past_memberships,
       count(*) FILTER (WHERE c.entry_kind = 'EXISTING') AS unknown_start_memberships
FROM travel_usage_cohorts c
LEFT JOIN plan_edits e ON e.plan_key = c.plan_key
GROUP BY c.entry_source
ORDER BY c.entry_source;
