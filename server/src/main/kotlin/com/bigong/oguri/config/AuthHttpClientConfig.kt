package com.bigong.oguri.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
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
        val requestFactory = SimpleClientHttpRequestFactory()
        requestFactory.setConnectTimeout(connectTimeoutMillis)
        requestFactory.setReadTimeout(readTimeoutMillis)
        return RestTemplate(requestFactory)
    }
}
