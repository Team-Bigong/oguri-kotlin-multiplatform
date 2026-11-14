ALTER TABLE travel_visits
    ADD COLUMN google_place_id VARCHAR(512),
    ALTER COLUMN name DROP NOT NULL,
    ADD CONSTRAINT travel_visit_place_source_check CHECK (
        (google_place_id IS NULL AND name IS NOT NULL)
        OR (google_place_id IS NOT NULL AND name IS NULL AND location IS NULL)
    );
