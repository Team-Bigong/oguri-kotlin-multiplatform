-- 저장된 여행지 테이블 생성
CREATE TABLE saved_destinations (
    id SERIAL PRIMARY KEY,
    member_id VARCHAR(100) NOT NULL,
    destination_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (member_id, destination_id), -- 중복 찜 방지
    CONSTRAINT fk_saved_dest_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
    CONSTRAINT fk_saved_dest_destination FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE
);
