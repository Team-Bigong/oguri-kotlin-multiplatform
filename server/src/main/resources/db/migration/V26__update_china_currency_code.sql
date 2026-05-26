-- 중국 위안화 코드를 수출입은행 API 규격(CNH)에 맞춰 업데이트
UPDATE countries SET currency_code = 'CNH' WHERE name = '중국';
