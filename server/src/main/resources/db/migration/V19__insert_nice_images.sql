-- 프랑스 니스 이미지 데이터 삽입
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Ffrance%2Fnice%2F1.jpg?alt=media&token=57f5b8c9-8128-43ae-b2cd-49fe7d1c18a7', 1, TRUE 
FROM destinations WHERE name = '니스';

INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Ffrance%2Fnice%2F2.jpg?alt=media&token=a9782023-6acd-4641-a05b-c3d53a433a0f', 2, FALSE 
FROM destinations WHERE name = '니스';

INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Ffrance%2Fnice%2F3.jpg?alt=media&token=24696d41-0e7d-4d8d-b49e-b755713a0db1', 3, FALSE 
FROM destinations WHERE name = '니스';
