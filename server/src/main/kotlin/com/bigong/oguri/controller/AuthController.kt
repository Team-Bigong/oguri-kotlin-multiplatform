package com.bigong.oguri.controller

import com.bigong.oguri.dto.AppleLoginRequest
import com.bigong.oguri.dto.GoogleLoginRequest
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
    private val memberService: MemberService,
) {
    @Operation(
        summary = "카카오 로그인",
        description = "카카오 액세스 토큰으로 로그인을 시도하고 서비스 전용 토큰을 발급합니다. 응답의 onboardingCompleted가 false면 온보딩 화면으로 이동해야 합니다. Authorization 헤더 없이 호출합니다.",
    )
    @PostMapping("/login/kakao")
    fun loginWithKakao(
        @RequestBody request: KakaoLoginRequest,
    ): LoginResponse = memberService.loginWithKakao(request.accessToken)

    @Operation(
        summary = "애플 로그인",
        description = "Apple identityToken으로 로그인을 시도하고 서비스 전용 토큰을 발급합니다. 응답의 onboardingCompleted가 false면 온보딩 화면으로 이동해야 합니다. Authorization 헤더 없이 호출합니다.",
    )
    @PostMapping("/login/apple")
    fun loginWithApple(
        @RequestBody request: AppleLoginRequest,
    ): LoginResponse = memberService.loginWithApple(request.identityToken)

    @Operation(
        summary = "구글 로그인",
        description = "Google identityToken으로 로그인을 시도하고 서비스 전용 토큰을 발급합니다. 응답의 onboardingCompleted가 false면 온보딩 화면으로 이동해야 합니다. Authorization 헤더 없이 호출합니다.",
    )
    @PostMapping("/login/google")
    fun loginWithGoogle(
        @RequestBody request: GoogleLoginRequest,
    ): LoginResponse = memberService.loginWithGoogle(request.identityToken)

    @Operation(summary = "토큰 재발급", description = "만료된 액세스 토큰을 리프레시 토큰으로 갱신합니다. Authorization 헤더 없이 호출합니다.")
    @PostMapping("/refresh")
    fun refresh(
        @RequestBody request: com.bigong.oguri.dto.TokenRefreshRequest,
    ): com.bigong.oguri.dto.TokenRefreshResponse = memberService.refreshAccessToken(request.refreshToken)

    @Operation(summary = "가짜 로그인 (로컬 테스트용)", description = "소셜 로그인 없이 특정 ID로 즉시 로그인을 시도합니다. 로컬 개발 환경에서만 사용하세요.")
    @PostMapping("/login/mock")
    fun loginWithMock(
        @RequestParam(defaultValue = "test_user") id: String,
    ): LoginResponse = memberService.loginWithMock(id)
}
