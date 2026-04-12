-- 1. countries 테이블에 currency_code 컬럼 추가
ALTER TABLE countries ADD COLUMN currency_code VARCHAR(3);

-- 2. 현재 등록된 9개 국가에 대한 통화 코드 업데이트
UPDATE countries SET currency_code = 'JPY' WHERE name = '일본';
UPDATE countries SET currency_code = 'PHP' WHERE name = '필리핀';
UPDATE countries SET currency_code = 'CNY' WHERE name = '중국';
UPDATE countries SET currency_code = 'EUR' WHERE name = '스페인';
UPDATE countries SET currency_code = 'USD' WHERE name = '미국';
UPDATE countries SET currency_code = 'VND' WHERE name = '베트남';
UPDATE countries SET currency_code = 'AUD' WHERE name = '호주';
UPDATE countries SET currency_code = 'EUR' WHERE name = '프랑스';
UPDATE countries SET currency_code = 'KRW' WHERE name = '대한민국';
