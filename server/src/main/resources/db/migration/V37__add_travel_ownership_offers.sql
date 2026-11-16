CREATE TABLE travel_ownership_offers (
    travel_plan_id UUID PRIMARY KEY REFERENCES travel_plans(id) ON DELETE CASCADE,
    target_id VARCHAR(100) NOT NULL REFERENCES members(id) ON DELETE CASCADE
);

ALTER TABLE travel_plans ADD COLUMN creator_id VARCHAR(100) REFERENCES members(id) ON DELETE SET NULL;
UPDATE travel_plans SET creator_id = owner_id;
ALTER TABLE travel_plans DROP CONSTRAINT travel_plan_creation_request_unique;
ALTER TABLE travel_plans ADD CONSTRAINT travel_plan_creation_request_unique UNIQUE (creator_id, creation_request_id);
