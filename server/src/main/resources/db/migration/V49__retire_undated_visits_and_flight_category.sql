-- Preserve existing visits and their order after removing the undated bucket.
WITH targets AS (
    SELECT p.id, (p.end_date - p.start_date + 1) AS last_day,
           COALESCE(MAX(v.position) FILTER (WHERE v.day_number = p.end_date - p.start_date + 1), -1) AS last_position
    FROM travel_plans p LEFT JOIN travel_visits v ON v.travel_plan_id = p.id
    GROUP BY p.id
), moved AS (
    SELECT v.id, t.last_day,
           t.last_position + ROW_NUMBER() OVER (PARTITION BY v.travel_plan_id ORDER BY v.position, v.id) AS position
    FROM travel_visits v JOIN targets t ON t.id = v.travel_plan_id
    WHERE v.day_number = 0
)
UPDATE travel_visits v SET day_number = m.last_day, position = m.position
FROM moved m WHERE v.id = m.id;
UPDATE travel_plan_views v SET day_number = p.end_date - p.start_date + 1
FROM travel_plans p WHERE v.travel_plan_id = p.id AND v.day_number = 0;
UPDATE travel_visits SET category = 'TRANSPORT' WHERE category = 'FLIGHT';
