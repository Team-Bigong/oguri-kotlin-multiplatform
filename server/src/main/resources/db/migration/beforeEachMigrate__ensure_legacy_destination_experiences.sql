-- V24 synchronizes this sequence, but legacy experience DDL was never versioned.
-- Bootstrap only the missing table after V1 creates destinations; leave existing data and checksums unchanged.
DO $$
BEGIN
    IF to_regclass('destinations') IS NOT NULL AND to_regclass('destination_experiences') IS NULL THEN
        CREATE TABLE destination_experiences (
            id SERIAL PRIMARY KEY,
            destination_id INTEGER NOT NULL,
            title VARCHAR(150) NOT NULL,
            description TEXT NOT NULL,
            thumbnail_url TEXT NOT NULL,
            link TEXT NOT NULL,
            sort_order INTEGER NOT NULL DEFAULT 1
        );
    END IF;
END;
$$;
