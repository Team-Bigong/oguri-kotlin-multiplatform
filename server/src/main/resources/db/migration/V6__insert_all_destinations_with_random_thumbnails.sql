-- 1. 중복 방지를 위한 UNIQUE 제약 조건 추가 (데이터 정합성을 위해 필수)
-- 이미 존재하는 데이터가 있을 수 있으므로 에러 방지용 처리 포함
DO $$ 
BEGIN 
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'countries_name_unique') THEN
        ALTER TABLE countries ADD CONSTRAINT countries_name_unique UNIQUE (name);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'destinations_name_unique') THEN
        ALTER TABLE destinations ADD CONSTRAINT destinations_name_unique UNIQUE (name);
    END IF;
END $$;

-- 2. 국가 데이터 삽입
INSERT INTO countries (name, big_mac_index) VALUES
('일본', 3.34), ('필리핀', 2.88), ('중국', 2.91), ('스페인', 4.76),
('미국', 5.27), ('베트남', 2.88), ('호주', 3.13), ('프랑스', 5.33), ('대한민국', 3.12)
ON CONFLICT (name) DO UPDATE SET big_mac_index = EXCLUDED.big_mac_index;

-- 3. 25개 여행지 데이터 삽입 (기존 오사카 제외 24개 + 오사카 업데이트)
INSERT INTO destinations (country_id, name, summary, description, recommend_start_month, recommend_end_month, flight_time)
SELECT id, '후쿠오카', '가깝고 먹을 게 많아서, 가볍게 다녀오기 좋아요', '가깝고 먹을 게 많아서, 가볍게 다녀오기 좋아요', 3, 5, '80분' FROM countries WHERE name = '일본' UNION ALL
SELECT id, '도쿄', '쇼핑도 전시도 디즈니도 있어서, 취향대로 일정 짜기 좋아요', '쇼핑도 전시도 디즈니도 있어서, 취향대로 일정 짜기 좋아요', 3, 5, '130분' FROM countries WHERE name = '일본' UNION ALL
SELECT id, '세부', '리조트에서 쉬고, 바다 투어까지 같이 즐기기 좋아요', '리조트에서 쉬고, 바다 투어까지 같이 즐기기 좋아요', 12, 4, '270분' FROM countries WHERE name = '필리핀' UNION ALL
SELECT id, '보라카이', '화이트 비치 물빛이 가장 또렷해지는 시기예요', '화이트 비치 물빛이 가장 또렷해지는 시기예요', 12, 4, '280분' FROM countries WHERE name = '필리핀' UNION ALL
SELECT id, '보홀', '한적한 바다와 자연 풍경을 함께 즐기기 좋아요', '한적한 바다와 자연 풍경을 함께 즐기기 좋아요', 12, 4, '270분' FROM countries WHERE name = '필리핀' UNION ALL
SELECT id, '상하이', '야경도 강하고, 도시 산책하기 좋은 분위기예요', '야경도 강하고, 도시 산책하기 좋은 분위기예요', 3, 5, '120분' FROM countries WHERE name = '중국' UNION ALL
SELECT id, '베이징', '자금성과 만리장성처럼 스케일 큰 여행이 가능해요', '자금성과 만리장성처럼 스케일 큰 여행이 가능해요', 4, 6, '130분' FROM countries WHERE name = '중국' UNION ALL
SELECT id, '칭다오', '바다 산책과 맥주 거리 분위기를 제대로 느끼기 좋아요', '바다 산책과 맥주 거리 분위기를 제대로 느끼기 좋아요', 6, 9, '90분' FROM countries WHERE name = '중국' UNION ALL
SELECT id, '바르셀로나', '가우디 건축과 바다 산책을 함께 즐기기 좋아요', '가우디 건축과 바다 산책을 함께 즐기기 좋아요', 4, 6, '800분' FROM countries WHERE name = '스페인' UNION ALL
SELECT id, '마드리드', '미술관도 강하고, 광장 분위기가 특히 좋아요', '미술관도 강하고, 광장 분위기가 특히 좋아요', 4, 6, '820분' FROM countries WHERE name = '스페인' UNION ALL
SELECT id, 'LA', '해변도 보고, 할리우드도 함께 즐기기 좋아요', '해변도 보고, 할리우드도 함께 즐기기 좋아요', 3, 5, '660분' FROM countries WHERE name = '미국' UNION ALL
SELECT id, '뉴욕', '브로드웨이도 보고, 도시 에너지를 가장 잘 느낄 수 있어요', '브로드웨이도 보고, 도시 에너지를 가장 잘 느낄 수 있어요', 4, 6, '840분' FROM countries WHERE name = '미국' UNION ALL
SELECT id, '샌프란시스코', '언덕과 바다 풍경이 가장 또렷해지는 시기예요', '언덕과 바다 풍경이 가장 또렷해지는 시기예요', 9, 10, '630분' FROM countries WHERE name = '미국' UNION ALL
SELECT id, '다낭', '바다에서 쉬고, 바나힐과 호이안까지 묶기 좋아요', '바다에서 쉬고, 바나힐과 호이안까지 묶기 좋아요', 2, 5, '280분' FROM countries WHERE name = '베트남' UNION ALL
SELECT id, '호치민', '카페 투어도 좋고, 로컬 먹거리가 확실해요', '카페 투어도 좋고, 로컬 먹거리가 확실해요', 12, 3, '310분' FROM countries WHERE name = '베트남' UNION ALL
SELECT id, '브리즈번', '강변 산책과 근교 여행을 같이 즐기기 좋아요', '강변 산책과 근교 여행을 같이 즐기기 좋아요', 3, 5, '600분' FROM countries WHERE name = '호주' UNION ALL
SELECT id, '시드니', '오페라하우스와 해변을 동시에 즐기기 좋은 계절이에요', '오페라하우스와 해변을 동시에 즐기기 좋은 계절이에요', 9, 11, '620분' FROM countries WHERE name = '호주' UNION ALL
SELECT id, '멜버른', '카페 문화와 골목 감성이 특히 살아나요', '카페 문화와 골목 감성이 특히 살아나요', 10, 3, '640분' FROM countries WHERE name = '호주' UNION ALL
SELECT id, '파리', '걷는 것만으로도 분위기가 완성돼요', '걷는 것만으로도 분위기가 완성돼요', 4, 6, '840분' FROM countries WHERE name = '프랑스' UNION ALL
SELECT id, '니스', '지중해 바다 색을 가장 선명하게 볼 수 있어요', '지중해 바다 색을 가장 선명하게 볼 수 있어요', 6, 9, '860분' FROM countries WHERE name = '프랑스' UNION ALL
SELECT id, '삿포로', '한여름에도 선선해서 걷기 좋아요', '한여름에도 선선해서 걷기 좋아요', 6, 8, '160분' FROM countries WHERE name = '일본' UNION ALL
SELECT id, '서울', '한국의 수도, 과거와 현재가 공존하는 곳', '한국의 수도, 과거와 현재가 공존하는 곳', 3, 5, '0분' FROM countries WHERE name = '대한민국' UNION ALL
SELECT id, '부산', '바다와 마천루가 어우러진 해양 도시', '바다와 마천루가 어우러진 해양 도시', 4, 6, '0분' FROM countries WHERE name = '대한민국' UNION ALL
SELECT id, '제주도', '사계절 내내 아름다운 한국의 대표 휴양지', '사계절 내내 아름다운 한국의 대표 휴양지', 3, 10, '0분' FROM countries WHERE name = '대한민국'
ON CONFLICT (name) DO UPDATE SET 
    summary = EXCLUDED.summary,
    description = EXCLUDED.description,
    recommend_start_month = EXCLUDED.recommend_start_month,
    recommend_end_month = EXCLUDED.recommend_end_month,
    flight_time = EXCLUDED.flight_time;

-- 4. 썸네일 랜덤 배정 (기존에 썸네일이 없는 경우에만 추가)
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 
       CASE (id % 3)
           WHEN 0 THEN 'https://media.triple.guide/triple-cms/c_limit,f_auto,h_1024,w_1024/74fdd210-d312-4aec-99de-d7900f4b95c0.jpeg'
           WHEN 1 THEN 'https://media.triple.guide/triple-cms/c_limit,f_auto,h_1024,w_1024/b41acf66-b33b-448c-8144-d9aba0df12c0.jpeg'
           ELSE 'https://ozimg.flyasiana.com/temp/image/20190417/249589c9-d0eb-4e2b-a0d2-c8cc84b94e14.jpeg'
       END,
       1, TRUE
FROM destinations d
WHERE NOT EXISTS (SELECT 1 FROM destination_images di WHERE di.destination_id = d.id AND di.is_thumbnail = TRUE);
