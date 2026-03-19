-- 1. total_trip_count 컬럼 추가 (기본값 0)
ALTER TABLE saved_recommendations ADD COLUMN total_trip_count INT NOT NULL DEFAULT 0;

-- 2. 기존 데이터가 있다면 날짜 차이로 채우기
UPDATE saved_recommendations SET total_trip_count = (end_date - start_date) + 1;
