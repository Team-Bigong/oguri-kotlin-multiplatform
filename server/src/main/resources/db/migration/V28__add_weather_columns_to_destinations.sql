-- destinations 테이블에 날씨 정보(평균 기온, 평균 강수량) 컬럼 추가
ALTER TABLE destinations ADD COLUMN weather_temp INTEGER;
ALTER TABLE destinations ADD COLUMN weather_precipitation_mm DOUBLE PRECISION;

-- 예시 데이터 업데이트 (mm 단위)
-- 후쿠오카 (3~5월 평균)
UPDATE destinations SET weather_temp = 16, weather_precipitation_mm = 70.5 WHERE id = 1;
-- 세부 (12~4월 평균)
UPDATE destinations SET weather_temp = 28, weather_precipitation_mm = 150.0 WHERE id = 4;
