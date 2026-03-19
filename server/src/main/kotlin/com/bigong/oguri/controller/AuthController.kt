package com.bigong.oguri.controller

import com.bigong.oguri.dto.AppleLoginRequest
import com.bigong.oguri.dto.KakaoLoginRequest
import com.bigong.oguri.dto.LoginResponse
import com.bigong.oguri.service.MemberService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@Tag(name = "Auth API", description = "인증 및 로그인 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val memberService: MemberService
) {
    @Operation(summary = "카카오 로그인", description = "카카오 액세스 토큰으로 로그인을 시도하고 서비스 전용 토큰을 발급합니다. Authorization 헤더 없이 호출합니다.")
    @PostMapping("/login/kakao")
    fun loginWithKakao(@RequestBody request: KakaoLoginRequest): LoginResponse {
        return memberService.loginWithKakao(request.accessToken)
    }

    @Operation(summary = "애플 로그인", description = "Apple identityToken으로 로그인을 시도하고 서비스 전용 토큰을 발급합니다. Authorization 헤더 없이 호출합니다.")
    @PostMapping("/login/apple")
    fun loginWithApple(@RequestBody request: AppleLoginRequest): LoginResponse {
        return memberService.loginWithApple(request.identityToken)
    }

    @Operation(summary = "토큰 재발급", description = "만료된 액세스 토큰을 리프레시 토큰으로 갱신합니다. Authorization 헤더 없이 호출합니다.")
    @PostMapping("/refresh")
    fun refresh(@RequestBody request: com.bigong.oguri.dto.TokenRefreshRequest): com.bigong.oguri.dto.TokenRefreshResponse {
        return memberService.refreshAccessToken(request.refreshToken)
    }
}
