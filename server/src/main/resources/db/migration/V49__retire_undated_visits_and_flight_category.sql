CREATE TEMP TABLE retired_travel_features ON COMMIT DROP AS
SELECT DISTINCT travel_plan_id FROM travel_visits WHERE day_number = 0 OR category = 'FLIGHT';
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

UPDATE travel_plans SET revision = revision + 1, structure_revision = structure_revision + 1
WHERE id IN (SELECT travel_plan_id FROM retired_travel_features);
UPDATE travel_visits v SET revision = p.revision
FROM travel_plans p WHERE v.travel_plan_id = p.id AND p.id IN (SELECT travel_plan_id FROM retired_travel_features);
INSERT INTO travel_day_revisions (travel_plan_id, day_number, revision)
SELECT p.id, p.end_date - p.start_date + 1, p.revision FROM travel_plans p
WHERE p.id IN (SELECT travel_plan_id FROM retired_travel_features)
ON CONFLICT (travel_plan_id, day_number) DO UPDATE SET revision = EXCLUDED.revision;
