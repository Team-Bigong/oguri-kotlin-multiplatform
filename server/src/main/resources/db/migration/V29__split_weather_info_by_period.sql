-- 1. 기존 weather_temp, weather_precipitation_mm 컬럼을 _1 버전으로 이름 변경
ALTER TABLE destinations RENAME COLUMN weather_temp TO weather_temp_1;
ALTER TABLE destinations RENAME COLUMN weather_precipitation_mm TO weather_precipitation_mm_1;

-- 2. 2차 추천 기간용 날씨 컬럼 추가
ALTER TABLE destinations ADD COLUMN weather_temp_2 INTEGER;
ALTER TABLE destinations ADD COLUMN weather_precipitation_mm_2 DOUBLE PRECISION;

-- 3. 예시 데이터 업데이트 (도쿄 등 복수 기간 장소)
-- 도쿄 (1차: 3~5월, 2차: 10~11월)
UPDATE destinations SET 
    weather_temp_1 = 15, weather_precipitation_mm_1 = 120.0,
    weather_temp_2 = 18, weather_precipitation_mm_2 = 150.0 
WHERE id = 3;
