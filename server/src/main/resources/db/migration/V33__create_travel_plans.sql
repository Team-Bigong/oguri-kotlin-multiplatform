CREATE TABLE travel_plans (
    id UUID PRIMARY KEY,
    owner_id VARCHAR(100) NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    creation_request_id UUID NOT NULL,
    creation_fingerprint VARCHAR(64) NOT NULL,
    city VARCHAR(100) NOT NULL CHECK (btrim(city) <> ''),
    time_zone VARCHAR(100) NOT NULL,
    title VARCHAR(100) NOT NULL CHECK (btrim(title) <> ''),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    revision BIGINT NOT NULL DEFAULT 0 CHECK (revision >= 0),
    CONSTRAINT travel_plan_dates_check CHECK (end_date - start_date BETWEEN 0 AND 365),
    CONSTRAINT travel_plan_creation_request_unique UNIQUE (owner_id, creation_request_id)
);

CREATE INDEX travel_plans_owner_dates_idx ON travel_plans (owner_id, start_date, id);

CREATE TABLE travel_visits (
    id UUID PRIMARY KEY,
    travel_plan_id UUID NOT NULL REFERENCES travel_plans(id) ON DELETE CASCADE,
    day_number INTEGER NOT NULL CHECK (day_number >= 0),
    position INTEGER NOT NULL CHECK (position >= 0),
    name VARCHAR(150) NOT NULL CHECK (btrim(name) <> ''),
    location VARCHAR(250),
    local_time TIME(0),
    note VARCHAR(1000),
    revision BIGINT NOT NULL CHECK (revision > 0),
    CONSTRAINT travel_visit_order_unique UNIQUE (travel_plan_id, day_number, position)
        DEFERRABLE INITIALLY DEFERRED
);

CREATE TABLE travel_day_revisions (
    travel_plan_id UUID NOT NULL REFERENCES travel_plans(id) ON DELETE CASCADE,
    day_number INTEGER NOT NULL CHECK (day_number >= 0),
    revision BIGINT NOT NULL CHECK (revision > 0),
    PRIMARY KEY (travel_plan_id, day_number)
);

CREATE TABLE travel_plan_commands (
    travel_plan_id UUID NOT NULL REFERENCES travel_plans(id) ON DELETE CASCADE,
    request_id UUID NOT NULL,
    fingerprint VARCHAR(64) NOT NULL,
    PRIMARY KEY (travel_plan_id, request_id)
);
