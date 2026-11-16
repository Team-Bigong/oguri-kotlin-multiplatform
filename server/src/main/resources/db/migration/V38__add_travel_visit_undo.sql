CREATE TABLE travel_visit_undo (
    travel_plan_id UUID NOT NULL REFERENCES travel_plans(id) ON DELETE CASCADE,
    member_id VARCHAR(100) NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    request_id UUID NOT NULL,
    revision BIGINT NOT NULL,
    payload TEXT NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (travel_plan_id, member_id, request_id)
);

CREATE INDEX travel_visit_undo_expiry ON travel_visit_undo (expires_at);
