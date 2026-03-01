-- 1. 국가 테이블
CREATE TABLE countries (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    big_mac_index DECIMAL(4, 2)
);

-- 2. 여행지 테이블
CREATE TABLE destinations (
    id SERIAL PRIMARY KEY,
    country_id INT,
    name VARCHAR(100) NOT NULL,
    flight_time VARCHAR(50),
    CONSTRAINT fk_destinations_country FOREIGN KEY (country_id) REFERENCES countries(id)
);

-- 3. 여행지 이미지 테이블 (3개 이상의 썸네일 관리)
CREATE TABLE destination_images (
    id SERIAL PRIMARY KEY,
    destination_id INT,
    image_url TEXT NOT NULL,
    sort_order INT DEFAULT 1, -- 1, 2, 3 순서 지정
    CONSTRAINT fk_destination_images_destination FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE
);

-- 4. 추천 정보 테이블
CREATE TABLE recommendations (
    id SERIAL PRIMARY KEY,
    destination_id INT,
    period_text VARCHAR(50),
    description TEXT,
    CONSTRAINT fk_recommendations_destination FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE
);
