-- Exposure, click, and login resume count sessions. Confirmed creation counts trips.
WITH counts AS (
    SELECT source, signed_in,
           count(*) FILTER (WHERE kind = 'EXPOSED') AS exposed_sessions,
           count(*) FILTER (WHERE kind = 'CLICKED') AS clicked_sessions,
           count(*) FILTER (WHERE kind = 'LOGIN_RESUMED') AS resumed_after_login_sessions,
           0::bigint AS created_trips
    FROM travel_recommendation_entries
    GROUP BY source, signed_in
    UNION ALL
    SELECT coalesce(recommendation_source, 'UNKNOWN'), true, 0, 0, 0, count(*)
    FROM travel_usage_cohorts
    WHERE entry_kind = 'CREATED' AND entry_source = 'RECOMMENDATION'
    GROUP BY recommendation_source
)
SELECT source, signed_in,
       sum(exposed_sessions)::bigint AS exposed_sessions,
       sum(clicked_sessions)::bigint AS clicked_sessions,
       sum(resumed_after_login_sessions)::bigint AS resumed_after_login_sessions,
       sum(created_trips)::bigint AS created_trips
FROM counts
GROUP BY source, signed_in
ORDER BY source, signed_in;
