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
import java.net.http.HttpClient
import java.time.Duration

@Configuration
class AuthHttpClientConfig(
    @param:Value("\${auth.http.connect-timeout-millis:2000}")
    private val connectTimeoutMillis: Int,
    @param:Value("\${auth.http.read-timeout-millis:3000}")
    private val readTimeoutMillis: Int,
) {
    @Bean
    fun authRestTemplate(): RestTemplate {
        val httpClient =
            HttpClient
                .newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeoutMillis.toLong()))
                .build()
        val requestFactory =
            JdkClientHttpRequestFactory(httpClient).apply {
                setReadTimeout(Duration.ofMillis(readTimeoutMillis.toLong()))
            }

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
            request.headers.set(
                "User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            )
            return execution.execute(request, body)
        }
    }
}
