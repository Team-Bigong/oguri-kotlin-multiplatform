ALTER TABLE travel_visits
    ADD COLUMN category varchar(20),
    ADD CONSTRAINT travel_visits_category_check CHECK (
        category IN ('RESTAURANT', 'CAFE', 'SIGHTSEEING', 'LODGING', 'FLIGHT', 'TRANSPORT', 'SHOPPING', 'OTHER')
    );
