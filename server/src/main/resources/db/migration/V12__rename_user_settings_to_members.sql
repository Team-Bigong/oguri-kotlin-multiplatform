-- 1. user_settings 테이블을 members로 변경
ALTER TABLE user_settings RENAME TO members;
ALTER TABLE members RENAME COLUMN user_id TO id;

-- 2. saved_recommendations 테이블의 user_id를 member_id로 변경
ALTER TABLE saved_recommendations RENAME COLUMN user_id TO member_id;
