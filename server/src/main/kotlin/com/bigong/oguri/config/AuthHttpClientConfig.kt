package com.bigong.oguri.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

@Configuration
class AuthHttpClientConfig(
    @param:Value("\${auth.http.connect-timeout-millis:2000}")
    private val connectTimeoutMillis: Int,
    @param:Value("\${auth.http.read-timeout-millis:3000}")
    private val readTimeoutMillis: Int,
) {
    @Bean
    fun authRestTemplate(): RestTemplate {
        val requestFactory = JdkClientHttpRequestFactory()
        // JdkClientHttpRequestFactory 에서는 내부적으로 타임아웃을 관리하므로 
        // 기본 설정을 사용하거나 필요 시 세부 튜닝이 가능합니다.
        
        val restTemplate = RestTemplate(requestFactory)
        restTemplate.interceptors.add(UserAgentInterceptor())
        return restTemplate
    }

    private class UserAgentInterceptor : ClientHttpRequestInterceptor {
        override fun intercept(
            request: HttpRequest,
            body: ByteArray,
            execution: ClientHttpRequestExecution,
        ): ClientHttpResponse {
            request.headers.set("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            return execution.execute(request, body)
        }
    }
}
