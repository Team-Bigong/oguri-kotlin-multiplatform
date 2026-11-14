package com.bigong.oguri.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

enum class TravelOperation { WRITE, INVITE }

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresTravelOperation(val value: TravelOperation)

@Component
class TravelOperationInterceptor(
    @param:Value("\${TRAVEL_WRITES_ENABLED:true}") private val writesEnabled: Boolean,
    @param:Value("\${TRAVEL_INVITES_ENABLED:true}") private val invitesEnabled: Boolean,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val operation = (handler as? HandlerMethod)?.getMethodAnnotation(RequiresTravelOperation::class.java)?.value
        val enabled =
            when (operation) {
                TravelOperation.WRITE -> writesEnabled
                TravelOperation.INVITE -> invitesEnabled
                null -> true
            }
        if (!enabled) {
            response.status = HttpStatus.SERVICE_UNAVAILABLE.value()
            response.setHeader(HttpHeaders.CACHE_CONTROL, CacheControl.noStore().headerValue)
        }
        return enabled
    }
}
