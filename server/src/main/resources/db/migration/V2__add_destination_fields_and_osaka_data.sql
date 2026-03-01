-- 1. 테이블 필드 추가
ALTER TABLE destinations ADD COLUMN summary VARCHAR(200);
ALTER TABLE destinations ADD COLUMN description TEXT;
ALTER TABLE destinations ADD COLUMN recommend_start_month INT;
ALTER TABLE destinations ADD COLUMN recommend_end_month INT;

ALTER TABLE destination_images ADD COLUMN is_thumbnail BOOLEAN DEFAULT FALSE;

-- 2. 일본 국가 데이터 삽입 (이미 있으면 무시)
INSERT INTO countries (name, big_mac_index) 
VALUES ('일본', 3.34);

-- 3. 오사카 여행지 데이터 삽입
-- (방금 넣은 일본의 ID를 조회해서 넣습니다)
INSERT INTO destinations (country_id, name, summary, description, recommend_start_month, recommend_end_month, flight_time)
SELECT id, '오사카', '유니버설 스튜디오도 있고, 먹방 코스도 탄탄해요', '유니버설 스튜디오도 있고, 먹방 코스도 탄탄해요', 3, 5, '95분'
FROM countries WHERE name = '일본' LIMIT 1;

-- 4. 오사카 이미지 데이터 삽입 (순서대로 썸네일, 디테일1, 디테일2)
-- (방금 넣은 오사카의 ID를 조회해서 넣습니다)
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://github.com/user-attachments/assets/24e9f0cc-5027-4418-9f48-ee3e19eb1784', 1, TRUE
FROM destinations WHERE name = '오사카' LIMIT 1;

INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://github.com/user-attachments/assets/78f1d0cc-13ef-4447-85bc-ed6ee4c27d43', 2, FALSE
FROM destinations WHERE name = '오사카' LIMIT 1;

INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://github.com/user-attachments/assets/a342d413-6ef4-4849-80ae-787d5fb68031', 3, FALSE
FROM destinations WHERE name = '오사카' LIMIT 1;
