-- Keep the visit identity after removal; current visit details are resolved only when reading.
ALTER TABLE travel_plan_changes ADD COLUMN visit_id UUID;
