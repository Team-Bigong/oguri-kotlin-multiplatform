-- 운영 중 수동 데이터 적재/복원 이후 시퀀스가 뒤처질 수 있어 PK 충돌 방지용 보정 수행
-- 각 테이블의 id 최대값 기준으로 시퀀스를 동기화한다.

SELECT setval(
    'destinations_id_seq',
    COALESCE((SELECT MAX(id) FROM destinations), 1),
    COALESCE((SELECT MAX(id) IS NOT NULL FROM destinations), FALSE)
);

SELECT setval(
    'destination_images_id_seq',
    COALESCE((SELECT MAX(id) FROM destination_images), 1),
    COALESCE((SELECT MAX(id) IS NOT NULL FROM destination_images), FALSE)
);

SELECT setval(
    'destination_experiences_id_seq',
    COALESCE((SELECT MAX(id) FROM destination_experiences), 1),
    COALESCE((SELECT MAX(id) IS NOT NULL FROM destination_experiences), FALSE)
);

SELECT setval(
    'countries_id_seq',
    COALESCE((SELECT MAX(id) FROM countries), 1),
    COALESCE((SELECT MAX(id) IS NOT NULL FROM countries), FALSE)
);

SELECT setval(
    'public_holidays_id_seq',
    COALESCE((SELECT MAX(id) FROM public_holidays), 1),
    COALESCE((SELECT MAX(id) IS NOT NULL FROM public_holidays), FALSE)
);
