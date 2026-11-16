-- V6 only checks constraint names across the entire database.
-- Prepare its constraints on the actual tables if another schema already owns those names.
DO $$
BEGIN
    IF to_regclass('flyway_schema_history') IS NULL THEN
        RETURN;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM flyway_schema_history WHERE version = '4' AND success)
       OR EXISTS (SELECT 1 FROM flyway_schema_history WHERE version = '6' AND success) THEN
        RETURN;
    END IF;

    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'countries_name_unique')
       AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'countries_name_unique' AND conrelid = 'countries'::regclass) THEN
        ALTER TABLE countries ADD CONSTRAINT countries_name_unique UNIQUE (name);
    END IF;
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'destinations_name_unique')
       AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'destinations_name_unique' AND conrelid = 'destinations'::regclass) THEN
        ALTER TABLE destinations ADD CONSTRAINT destinations_name_unique UNIQUE (name);
    END IF;
END;
$$;
