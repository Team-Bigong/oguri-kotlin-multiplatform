package com.bigong.oguri.controller

import com.bigong.oguri.dto.AdminLoginRequest
import com.bigong.oguri.dto.AdminLoginResponse
import com.bigong.oguri.service.AdminAuthService
import io.swagger.v3.oas.annotations.Hidden
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Hidden
@RestController
@RequestMapping("/api/admin/v1/auth")
class AdminAuthController(
    private val adminAuthService: AdminAuthService,
) {
    @PostMapping("/login")
    fun login(
        @RequestBody request: AdminLoginRequest,
    ): AdminLoginResponse {
        val (accessToken, expiresInSeconds) = adminAuthService.login(request.username, request.password)
        return AdminLoginResponse(
            accessToken = accessToken,
            expiresInSeconds = expiresInSeconds,
        )
    }
}
