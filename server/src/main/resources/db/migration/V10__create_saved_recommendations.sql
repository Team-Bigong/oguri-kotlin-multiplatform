-- 저장된 연휴 테이블 생성
CREATE TABLE saved_recommendations (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(100) DEFAULT 'GUEST', -- 추후 로그인이 도입되면 실제 유저 ID 사용
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    day_off_count INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, start_date, end_date) -- 동일 기간 중복 저장 방지
);
