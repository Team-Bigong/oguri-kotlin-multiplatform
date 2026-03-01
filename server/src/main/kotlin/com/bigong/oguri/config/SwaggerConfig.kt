package com.bigong.oguri.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Oguri API Server")
                    .version("v1.0.0")
                    .description("여행지 추천 서비스 Oguri의 API 문서입니다.")
            )
    }
}
