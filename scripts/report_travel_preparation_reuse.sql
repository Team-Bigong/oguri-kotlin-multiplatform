-- Same 8-day / 24-hour / 7-day cohort definition as the product specification.
-- Never infer a session for legacy clients, or treat display/push opening as purposeful reuse.
WITH observed AS (
    SELECT c.*,
           c.days_until_start >= 8 AND c.started_at + interval '7 days' <= statement_timestamp() AS matured,
           EXISTS (
               SELECT 1 FROM travel_usage_activity a
               WHERE a.cohort_id = c.id AND a.session_id <> c.start_session_id
                 AND a.kind <> 'PLAN_DISPLAYED'
                 AND a.occurred_at >= c.started_at + interval '24 hours'
                 AND a.occurred_at <= c.started_at + interval '7 days'
           ) OR EXISTS (
               SELECT 1 FROM travel_usage_edits e
               WHERE e.cohort_id = c.id AND e.session_id <> c.start_session_id
                 AND e.occurred_at >= c.started_at + interval '24 hours'
                 AND e.occurred_at <= c.started_at + interval '7 days'
           ) AS purposeful_reuse,
           EXISTS (
               SELECT 1 FROM travel_usage_activity a
               WHERE a.cohort_id = c.id AND a.session_id <> c.start_session_id
                 AND a.kind = 'PLAN_DISPLAYED'
                 AND a.occurred_at >= c.started_at + interval '24 hours'
                 AND a.occurred_at <= c.started_at + interval '7 days'
           ) AS displayed_again
    FROM travel_usage_cohorts c
)
SELECT coalesce(entry_source, 'ALL') AS entry_source,
       count(*) FILTER (WHERE matured) AS eligible_memberships,
       count(*) FILTER (WHERE matured AND start_session_id IS NOT NULL) AS session_known_memberships,
       count(*) FILTER (WHERE matured AND purposeful_reuse) AS reused_memberships,
       count(*) FILTER (WHERE matured AND displayed_again) AS displayed_again_memberships,
       count(DISTINCT member_id) FILTER (WHERE matured AND start_session_id IS NOT NULL) AS eligible_linked_users,
       count(DISTINCT member_id) FILTER (WHERE matured AND purposeful_reuse) AS reused_linked_users,
       count(*) FILTER (WHERE matured AND member_id IS NULL) AS unlinked_eligible_memberships,
       count(*) FILTER (WHERE days_until_start >= 8 AND NOT matured) AS pending_memberships,
       count(*) FILTER (WHERE entry_kind = 'EXISTING') AS unknown_start_memberships
FROM observed
GROUP BY GROUPING SETS ((entry_source), ())
ORDER BY entry_source;
