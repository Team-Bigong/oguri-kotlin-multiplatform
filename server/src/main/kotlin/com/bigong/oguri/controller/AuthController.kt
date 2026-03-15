package com.bigong.oguri.controller

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
    @Operation(summary = "카카오 로그인", description = "카카오 액세스 토큰으로 로그인을 시도하고 서비스 전용 토큰을 발급합니다.")
    @PostMapping("/login/kakao")
    fun loginWithKakao(@RequestBody request: KakaoLoginRequest): LoginResponse {
        return memberService.loginWithKakao(request.accessToken)
    }
}
