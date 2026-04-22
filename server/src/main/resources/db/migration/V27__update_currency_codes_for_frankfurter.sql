-- Frankfurter API(표준 ISO) 규격에 맞춰 통화 코드 최종 정리
UPDATE countries SET currency_code = 'CNY' WHERE name = '중국';
UPDATE countries SET currency_code = 'PHP' WHERE name = '필리핀';
UPDATE countries SET currency_code = 'VND' WHERE name = '베트남' AND currency_code IS NULL;
