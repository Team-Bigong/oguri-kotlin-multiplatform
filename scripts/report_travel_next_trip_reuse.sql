-- Use the first qualifying ended trip per linked account in the observation period.
-- Output only aggregates. Unlinked memberships cannot establish a user denominator.
WITH report_period AS (
    SELECT coalesce(nullif(current_setting('oguri.travel_observation_start', true), '')::timestamptz, '-infinity') AS starts_at,
           least(coalesce(nullif(current_setting('oguri.travel_observation_end', true), '')::timestamptz, 'infinity'),
                 statement_timestamp()) AS observed_through
),
ended_memberships AS (
    SELECT c.member_id, c.initial_end_at, p.observed_through
    FROM travel_usage_cohorts c CROSS JOIN report_period p
    WHERE c.days_until_start >= 8
      AND c.started_at + interval '168 hours' <= c.initial_end_at
      AND c.initial_end_at >= p.starts_at
      AND c.initial_end_at <= p.observed_through
),
first_end AS (
    SELECT member_id, min(initial_end_at) AS ended_at, observed_through
    FROM ended_memberships
    WHERE member_id IS NOT NULL
    GROUP BY member_id, observed_through
),
windows AS (
    SELECT member_id, ended_at, ended_at + interval '2160 hours' AS window_end, observed_through
    FROM first_end
),
outcomes AS (
    SELECT w.member_id, w.window_end <= w.observed_through AS matured,
           EXISTS (
               SELECT 1
               FROM travel_usage_cohorts c
               JOIN travel_usage_activity a ON a.cohort_id = c.id
               WHERE c.member_id = w.member_id AND a.kind = 'NEXT_HOLIDAY_OPENED'
                 AND a.occurred_at > w.ended_at AND a.occurred_at <= w.window_end
           ) AS explored,
           EXISTS (
               SELECT 1 FROM travel_usage_cohorts c
               WHERE c.member_id = w.member_id AND c.entry_kind = 'CREATED'
                 AND c.started_at > w.ended_at AND c.started_at <= w.window_end
           ) AS created,
           EXISTS (
               SELECT 1 FROM travel_usage_cohorts c
               WHERE c.member_id = w.member_id AND c.entry_kind = 'JOINED'
                 AND c.started_at > w.ended_at AND c.started_at <= w.window_end
           ) AS joined
    FROM windows w
)
SELECT count(*) FILTER (WHERE matured) AS eligible_linked_users,
       count(*) FILTER (WHERE NOT matured) AS pending_linked_users,
       count(*) FILTER (WHERE matured AND explored) AS explored_linked_users,
       count(*) FILTER (WHERE matured AND created) AS created_linked_users,
       count(*) FILTER (WHERE matured AND joined) AS joined_linked_users,
       count(*) FILTER (WHERE matured AND (created OR joined)) AS started_next_trip_linked_users,
       (SELECT count(*) FROM ended_memberships WHERE member_id IS NULL) AS unlinked_ended_memberships
FROM outcomes;
