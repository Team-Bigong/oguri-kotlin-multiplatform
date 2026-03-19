package com.bigong.oguri.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.tags.Tag
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun openAPI(): OpenAPI {
        val securitySchemeName = "bearerAuth"

        return OpenAPI()
            .info(
                Info()
                    .title("Oguri API Server")
                    .version("v1.0.0")
                    .description(
                        "여행지 추천 서비스 Oguri의 API 문서입니다.\n" +
                            "- 로그인/토큰 재발급 API는 Authorization 헤더 없이 호출합니다.\n" +
                            "- 그 외 API는 Authorization: Bearer {accessToken} 사용을 권장합니다.\n" +
                            "- 로그인 응답의 onboardingCompleted가 false이면 /api/v1/members/onboarding 호출 후 앱 진입을 허용합니다.\n" +
                            "- 게스트 둘러보기는 조회 API 일부에서 동작하며, 저장/마이페이지 기능은 로그인 필요(401)입니다."
                    )
            )
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
            .components(
                Components()
                    .addSecuritySchemes(
                        securitySchemeName,
                        SecurityScheme()
                            .name(securitySchemeName)
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                    )
            )
    }
}
