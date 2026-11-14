package com.bigong.oguri.config

import jakarta.servlet.http.HttpServletRequest
import java.util.UUID

fun HttpServletRequest.resolveTravelUsageSession(): UUID? = getHeader("X-Travel-Session")?.let { UUID.fromString(it) }
