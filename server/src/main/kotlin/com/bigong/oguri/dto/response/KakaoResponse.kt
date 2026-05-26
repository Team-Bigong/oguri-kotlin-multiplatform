package com.bigong.oguri.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 카카오 서버로부터 받는 유저 정보 응답
 */
data class KakaoUserInfoResponse(
    val id: Long,
    @JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccount?,
)

data class KakaoAccount(
    val profile: KakaoProfile?,
)

data class KakaoProfile(
    val nickname: String?,
    @JsonProperty("thumbnail_image_url")
    val thumbnailImageUrl: String?,
)
