CREATE TABLE travel_plan_cities (
    travel_plan_id UUID NOT NULL REFERENCES travel_plans(id) ON DELETE CASCADE,
    city_order INTEGER NOT NULL,
    city VARCHAR(100) NOT NULL,
    PRIMARY KEY (travel_plan_id, city_order),
    UNIQUE (travel_plan_id, city)
);
INSERT INTO travel_plan_cities (travel_plan_id, city_order, city)
SELECT id, 0, city FROM travel_plans;
