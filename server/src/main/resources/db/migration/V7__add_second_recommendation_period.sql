-- 1. 기존 컬럼 이름 변경 및 새 컬럼 추가
ALTER TABLE destinations RENAME COLUMN recommend_start_month TO recommend_start_month_1;
ALTER TABLE destinations RENAME COLUMN recommend_end_month TO recommend_end_month_1;

ALTER TABLE destinations ADD COLUMN recommend_start_month_2 INT;
ALTER TABLE destinations ADD COLUMN recommend_end_month_2 INT;
