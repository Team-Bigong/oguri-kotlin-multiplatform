-- 1. 기존 day_off_count를 remaining_day_off로 변경
ALTER TABLE members RENAME COLUMN day_off_count TO remaining_day_off;

-- 2. preferred_day_off 컬럼 추가 (기본값 3)
ALTER TABLE members ADD COLUMN preferred_day_off INT NOT NULL DEFAULT 3;
