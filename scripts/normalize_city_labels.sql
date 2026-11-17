-- Run after importing GeoNames. Preserve old names as search aliases.
BEGIN;
LOCK TABLE cities, city_aliases IN SHARE ROW EXCLUSIVE MODE;

CREATE TEMP TABLE city_label_updates ON COMMIT DROP AS
WITH short_names AS (
    SELECT id, name, region_label,
           CASE
               WHEN name ~ '^[가-힣]{2,}(특별자치시|특별시|광역시| +시)$'
                   THEN regexp_replace(name, '(특별자치시|특별시|광역시| +시)$', '')
               WHEN country_code = 'KR' AND name ~ '^[가-힣]{2,}시$'
                   THEN regexp_replace(name, '시$', '')
               ELSE name
           END AS short_name
    FROM cities
), labels AS (
    SELECT s.*,
           CASE
               WHEN region_label ~ '^[^·]+ · [^·]+$' AND (
                   lower(split_part(region_label, ' · ', 2)) IN (lower(name), lower(short_name))
                   OR EXISTS (
                       SELECT FROM city_aliases a
                       WHERE a.city_id = s.id
                         AND lower(a.name) = lower(split_part(s.region_label, ' · ', 2))
                   )
               ) THEN split_part(region_label, ' · ', 1)
               ELSE region_label
           END AS short_region
    FROM short_names s
), candidates AS (
    SELECT *, count(*) OVER (PARTITION BY short_name, short_region) AS matches
    FROM labels
)
SELECT id, name, region_label, short_name, short_region
FROM candidates x
WHERE (name, region_label) IS DISTINCT FROM (short_name, short_region)
  AND matches = 1
  AND NOT EXISTS (
      SELECT FROM cities c WHERE c.id <> x.id
        AND (c.name || ' · ' || c.region_label) = (x.short_name || ' · ' || x.short_region)
  );

INSERT INTO city_aliases (city_id, name)
SELECT id, name FROM city_label_updates WHERE name <> short_name
UNION
SELECT id, name || ' · ' || region_label FROM city_label_updates
ON CONFLICT DO NOTHING;

UPDATE cities c
SET name = x.short_name, region_label = x.short_region
FROM city_label_updates x
WHERE c.id = x.id;

COMMIT;
