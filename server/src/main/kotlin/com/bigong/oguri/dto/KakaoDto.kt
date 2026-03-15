package com.bigong.oguri.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 클라이언트로부터 받는 카카오 로그인 요청
 */
data class KakaoLoginRequest(
    val accessToken: String
)

/**
 * 카카오 서버로부터 받는 유저 정보 응답
 */
data class KakaoUserInfoResponse(
    val id: Long,
    @JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccount?
)

data class KakaoAccount(
    val profile: KakaoProfile?
)

data class KakaoProfile(
    val nickname: String?,
    @JsonProperty("thumbnail_image_url")
    val thumbnailImageUrl: String?
)
