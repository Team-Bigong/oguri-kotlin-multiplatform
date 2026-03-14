-- 1. 실제 공휴일 당일 여부 컬럼 추가
ALTER TABLE public_holidays ADD COLUMN is_actual_holiday BOOLEAN DEFAULT TRUE;

-- 2. '연휴' 또는 '대체공휴일'이 포함된 공휴일은 당일이 아님으로 표시
UPDATE public_holidays SET is_actual_holiday = FALSE WHERE name LIKE '%연휴%';
UPDATE public_holidays SET is_actual_holiday = FALSE WHERE name LIKE '%대체공휴일%';
