-- Some installations previously added these destination fields outside Flyway.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_attribute
        WHERE attrelid = 'destinations'::regclass AND attname = 'flight_time_minutes' AND NOT attisdropped
    ) THEN
        ALTER TABLE destinations ADD COLUMN flight_time_minutes INTEGER;
        IF EXISTS (
            SELECT 1 FROM pg_attribute
            WHERE attrelid = 'destinations'::regclass AND attname = 'flight_time' AND NOT attisdropped
        ) THEN
            UPDATE destinations
            SET flight_time_minutes = substring(flight_time FROM '^[[:space:]]*([0-9]{1,4})[[:space:]]*분[[:space:]]*$')::integer
            WHERE flight_time ~ '^[[:space:]]*[0-9]{1,4}[[:space:]]*분[[:space:]]*$';
        END IF;
    END IF;
END;
$$;

ALTER TABLE destinations ADD COLUMN IF NOT EXISTS flight_url TEXT;
