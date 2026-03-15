-- 1. 기존 이미지 데이터 삭제
TRUNCATE TABLE destination_images RESTART IDENTITY;

-- 2. 후쿠오카 이미지
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fjapan%2Ffukuoka%2F1.png?alt=media&token=ff8d08b1-551b-4798-aaf0-d47f4040c21d', 1, TRUE FROM destinations WHERE name = '후쿠오카';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fjapan%2Ffukuoka%2F2.png?alt=media&token=b88180f5-865b-4303-9437-d4ea2e5344c2', 2, FALSE FROM destinations WHERE name = '후쿠오카';

-- 3. 오사카 이미지
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fjapan%2Fosaka%2F1.jpg?alt=media&token=d92d913c-f0ab-477a-8abc-8406cb1622d3', 1, TRUE FROM destinations WHERE name = '오사카';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fjapan%2Fosaka%2F2.jpg?alt=media&token=b60b4483-bfde-47c1-8651-507a04397202', 2, FALSE FROM destinations WHERE name = '오사카';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fjapan%2Fosaka%2F3.jpeg?alt=media&token=eb096012-82f6-411a-849e-fa2a0975a407', 3, FALSE FROM destinations WHERE name = '오사카';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fjapan%2Fosaka%2F4.png?alt=media&token=6373de48-3b5b-4e2c-bf1a-61f1f943fba2', 4, FALSE FROM destinations WHERE name = '오사카';

-- 4. 도쿄 이미지
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fjapan%2Ftokyo%2F1.jpg?alt=media&token=9f0346ef-235c-460a-b112-6b70627476ad', 1, TRUE FROM destinations WHERE name = '도쿄';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fjapan%2Ftokyo%2F2.jpg?alt=media&token=5380ad73-fd8c-4bcd-b2c2-799f01280126', 2, FALSE FROM destinations WHERE name = '도쿄';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fjapan%2Ftokyo%2F3.png?alt=media&token=dd45b23b-29af-4cbd-a946-761e80966e47', 3, FALSE FROM destinations WHERE name = '도쿄';

-- 5. 삿포로 이미지
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fjapan%2Fsapporo%2F1.png?alt=media&token=0a3361dc-b787-4a04-8255-12523cf1ad13', 1, TRUE FROM destinations WHERE name = '삿포로';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fjapan%2Fsapporo%2F2.png?alt=media&token=6c289d0d-402b-49b8-9ff5-9521de05b97d', 2, FALSE FROM destinations WHERE name = '삿포로';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fjapan%2Fsapporo%2F3.png?alt=media&token=c6ac0d5a-c4fc-45d6-b194-60646857db5f', 3, FALSE FROM destinations WHERE name = '삿포로';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fjapan%2Fsapporo%2F4.png?alt=media&token=2b19deaa-f213-4fad-a8b2-ecf2ddec3134', 4, FALSE FROM destinations WHERE name = '삿포로';

-- 6. 보라카이 이미지
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fphilippines%2Fboracay%2F1.jpg?alt=media&token=c73bbd56-3aab-427d-9baf-5f8f0247249b', 1, TRUE FROM destinations WHERE name = '보라카이';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fphilippines%2Fboracay%2F2.jpg?alt=media&token=40b24065-9817-409c-a129-12853a367886', 2, FALSE FROM destinations WHERE name = '보라카이';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fphilippines%2Fboracay%2F3.jpg?alt=media&token=d4f0a915-5a5e-4486-b38d-7648757fdeb2', 3, FALSE FROM destinations WHERE name = '보라카이';

-- 7. 보홀 이미지
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fphilippines%2Fbohol%2F1.jpg?alt=media&token=24c93c61-4441-484c-b9ae-a430b71b5e18', 1, TRUE FROM destinations WHERE name = '보홀';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fphilippines%2Fbohol%2F2.jpg?alt=media&token=53705758-308d-4f35-a2d2-913ad659bc7a', 2, FALSE FROM destinations WHERE name = '보홀';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fphilippines%2Fbohol%2F3.jpg?alt=media&token=a407c1f8-ca4d-4bef-9836-bb82ca2019c3', 3, FALSE FROM destinations WHERE name = '보홀';

-- 8. 상하이 이미지
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fchina%2Fshanghai%2F1.png?alt=media&token=d79562bf-2920-4683-a0fa-a9352cec0ea0', 1, TRUE FROM destinations WHERE name = '상하이';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fchina%2Fshanghai%2F2.png?alt=media&token=3f3d8d66-fece-46b1-84a2-ad47cc71daa0', 2, FALSE FROM destinations WHERE name = '상하이';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fchina%2Fshanghai%2F3.jpeg?alt=media&token=11adb907-ff39-4182-b81f-eda2dc689e3c', 3, FALSE FROM destinations WHERE name = '상하이';

-- 9. 베이징 이미지
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fchina%2Fbeijing%2F1.jpg?alt=media&token=90544fd4-3722-4363-8548-bb7832a80a19', 1, TRUE FROM destinations WHERE name = '베이징';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fchina%2Fbeijing%2F2.jpg?alt=media&token=0e809a23-9a45-44da-a5a2-bd18248af724', 2, FALSE FROM destinations WHERE name = '베이징';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fchina%2Fbeijing%2F3.jpg?alt=media&token=0fdd8b10-74b1-4a83-8a2e-b318a0012416', 3, FALSE FROM destinations WHERE name = '베이징';

-- 10. 칭다오 이미지
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fchina%2Fqingdao%2F1.png?alt=media&token=d1e8e607-bb11-42dd-8255-06fc0a55b4a6', 1, TRUE FROM destinations WHERE name = '칭다오';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fchina%2Fqingdao%2F2.png?alt=media&token=d34275bb-e5cf-4a03-bce5-338beca000ad', 2, FALSE FROM destinations WHERE name = '칭다오';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fchina%2Fqingdao%2F3.png?alt=media&token=49b53449-16c1-4f52-8e96-15a5289fb3b8', 3, FALSE FROM destinations WHERE name = '칭다오';

-- 11. 바르셀로나 이미지
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fspain%2Fbarcelona%2F1.jpg?alt=media&token=1df2436b-3dab-4db8-b5e3-06d4b505e8c9', 1, TRUE FROM destinations WHERE name = '바르셀로나';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fspain%2Fbarcelona%2F2.jpg?alt=media&token=652ea0de-b69c-480a-8c7b-54e5f67e3a20', 2, FALSE FROM destinations WHERE name = '바르셀로나';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fspain%2Fbarcelona%2F3.jpg?alt=media&token=8902502d-a6de-416c-8f08-36bb28d41e26', 3, FALSE FROM destinations WHERE name = '바르셀로나';

-- 12. 마드리드 이미지
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fspain%2Fmadrid%2F1.jpg?alt=media&token=b095e4cf-53c7-429a-bd64-46099c5a9b94', 1, TRUE FROM destinations WHERE name = '마드리드';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fspain%2Fmadrid%2F2.jpg?alt=media&token=416d7cca-11dd-4ba3-a0dd-5d6487796003', 2, FALSE FROM destinations WHERE name = '마드리드';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fspain%2Fmadrid%2F3.jpg?alt=media&token=5833169c-1373-44ec-aa17-2933d1f8096c', 3, FALSE FROM destinations WHERE name = '마드리드';

-- 13. LA 이미지
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Funited-states%2Flos-angeles%2F1.png?alt=media&token=ae7eaee7-fbb1-46b4-a893-922b5857cf00', 1, TRUE FROM destinations WHERE name = 'LA';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Funited-states%2Flos-angeles%2F2.png?alt=media&token=b026dbdb-a12e-4087-a89f-ec9229f0be1d', 2, FALSE FROM destinations WHERE name = 'LA';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Funited-states%2Flos-angeles%2F3.png?alt=media&token=97c8720f-4c43-48ac-8920-ee67e572492c', 3, FALSE FROM destinations WHERE name = 'LA';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Funited-states%2Flos-angeles%2F4.png?alt=media&token=729a75e6-04f9-413a-9de0-7f929dd6afe6', 4, FALSE FROM destinations WHERE name = 'LA';

-- 14. 뉴욕 이미지
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Funited-states%2Fnew-york%2F1.png?alt=media&token=653e8941-676a-4812-985a-bbb9498f7a00', 1, TRUE FROM destinations WHERE name = '뉴욕';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Funited-states%2Fnew-york%2F2.png?alt=media&token=d64f3445-d72b-4b37-b5cb-3486d626f604', 2, FALSE FROM destinations WHERE name = '뉴욕';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Funited-states%2Fnew-york%2F3.png?alt=media&token=219f62d2-94f1-4df5-9cde-1e8f9ba342a4', 3, FALSE FROM destinations WHERE name = '뉴욕';

-- 15. 샌프란시스코 이미지
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Funited-states%2Fsan-francisco%2F2.png?alt=media&token=13106724-c1c7-417b-8276-5a1a4d604426', 1, TRUE FROM destinations WHERE name = '샌프란시스코';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Funited-states%2Fsan-francisco%2F1.png?alt=media&token=a3c8d639-93a5-4b11-8b94-737b1d1dc1ba', 2, FALSE FROM destinations WHERE name = '샌프란시스코';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Funited-states%2Fsan-francisco%2F3.png?alt=media&token=074a2ba6-cab1-4825-89ff-5ada1dccbab6', 3, FALSE FROM destinations WHERE name = '샌프란시스코';

-- 16. 브리즈번 이미지
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Faustralia%2Fbrisbane%2F1.jpg?alt=media&token=ef526233-e426-4c7d-93b8-deb8b12c70ca', 1, TRUE FROM destinations WHERE name = '브리즈번';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Faustralia%2Fbrisbane%2F2.jpg?alt=media&token=f7b0b1a2-7a87-4096-a040-002d668ed9bf', 2, FALSE FROM destinations WHERE name = '브리즈번';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Faustralia%2Fbrisbane%2F3.jpg?alt=media&token=14d5012f-38bf-4bbc-8faf-c09ba70dacef', 3, FALSE FROM destinations WHERE name = '브리즈번';

-- 17. 시드니 이미지
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Faustralia%2Fsydney%2F1.jpg?alt=media&token=cd55eb8d-4135-4b7d-b637-18f788b9f3d8', 1, TRUE FROM destinations WHERE name = '시드니';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Faustralia%2Fsydney%2F2.jpg?alt=media&token=2aa0a6aa-9172-411a-8786-97dbe4d9f500', 2, FALSE FROM destinations WHERE name = '시드니';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Faustralia%2Fsydney%2F3.jpg?alt=media&token=92425d27-fecc-40ad-95ed-a6a3d5ca5681', 3, FALSE FROM destinations WHERE name = '시드니';

-- 18. 멜버른 이미지
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Faustralia%2Fmelbourne%2F1.jpg?alt=media&token=125e7812-a1ef-4446-95a0-d59dd9b38e80', 1, TRUE FROM destinations WHERE name = '멜버른';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Faustralia%2Fmelbourne%2F2.jpg?alt=media&token=a28c5f8e-7919-4853-8fbc-a86d93902fca', 2, FALSE FROM destinations WHERE name = '멜버른';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Faustralia%2Fmelbourne%2F3.jpg?alt=media&token=d0317dbf-5882-4b6f-8593-a605350009d2', 3, FALSE FROM destinations WHERE name = '멜버른';

-- 19. 파리 이미지
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Ffrance%2Fparis%2F1.png?alt=media&token=226c4861-7e78-4d22-b353-22483e7dd94a', 1, TRUE FROM destinations WHERE name = '파리';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Ffrance%2Fparis%2F2.jpg?alt=media&token=e6e9d16b-102b-40a5-a728-ff61424aa468', 2, FALSE FROM destinations WHERE name = '파리';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Ffrance%2Fparis%2F3.png?alt=media&token=6787f71a-f1a0-479b-bc0b-1eb2b81d2c34', 3, FALSE FROM destinations WHERE name = '파리';

-- 20. 서울 이미지
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fsouth-korea%2Fseoul%2F1.png?alt=media&token=368d8675-659a-49fd-91d4-0087971ee83b', 1, TRUE FROM destinations WHERE name = '서울';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fsouth-korea%2Fseoul%2F2.png?alt=media&token=d82a812b-388c-4d53-b4cc-b70cc0e8c8fc', 2, FALSE FROM destinations WHERE name = '서울';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fsouth-korea%2Fseoul%2F3.png?alt=media&token=0583d2e9-67ee-42f6-8b0e-a3294dc7d31d', 3, FALSE FROM destinations WHERE name = '서울';

-- 21. 부산 이미지
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fsouth-korea%2Fbusan%2F1.png?alt=media&token=461d8001-05ec-4aa0-8760-0539d467139b', 1, TRUE FROM destinations WHERE name = '부산';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fsouth-korea%2Fbusan%2F2.png?alt=media&token=6730fb55-083a-4ca4-91f8-4d4e560ca987', 2, FALSE FROM destinations WHERE name = '부산';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fsouth-korea%2Fbusan%2F3.png?alt=media&token=5869ca15-efd2-40d2-a552-a52088cc8bc7', 3, FALSE FROM destinations WHERE name = '부산';

-- 22. 제주도 이미지
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fsouth-korea%2Fjeju%2F1.png?alt=media&token=e8be851a-fdcd-4b1d-91bc-e111f2e819fd', 1, TRUE FROM destinations WHERE name = '제주도';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fsouth-korea%2Fjeju%2F2.png?alt=media&token=4ef5199a-9110-4500-a32e-436b284f8efe', 2, FALSE FROM destinations WHERE name = '제주도';
INSERT INTO destination_images (destination_id, image_url, sort_order, is_thumbnail)
SELECT id, 'https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/places%2Fsouth-korea%2Fjeju%2F3.png?alt=media&token=d9cee4fd-bcd7-4d27-bdfa-140115fa7602', 3, FALSE FROM destinations WHERE name = '제주도';
