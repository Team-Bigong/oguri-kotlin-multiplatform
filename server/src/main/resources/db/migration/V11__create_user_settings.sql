-- 유저 설정 테이블 생성
CREATE TABLE user_settings (
    user_id VARCHAR(100) PRIMARY KEY,
    day_off_count INT NOT NULL DEFAULT 3,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 기본 테스트 데이터 (GUEST 유저용)
INSERT INTO user_settings (user_id, day_off_count) VALUES ('GUEST', 3)
ON CONFLICT (user_id) DO NOTHING;
