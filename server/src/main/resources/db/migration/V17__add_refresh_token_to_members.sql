-- members 테이블에 리프레시 토큰 컬럼 추가
ALTER TABLE members ADD COLUMN refresh_token TEXT;
