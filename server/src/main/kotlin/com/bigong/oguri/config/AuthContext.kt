package com.bigong.oguri.config

import jakarta.servlet.http.HttpServletRequest

object AuthContext {
    const val AUTHENTICATED_MEMBER_ID_ATTRIBUTE: String = "authenticatedMemberId"
    const val GUEST_MEMBER_ID: String = "GUEST"
}

fun HttpServletRequest.resolveMemberId(): String {
    val resolvedMemberId = this.getAttribute(AuthContext.AUTHENTICATED_MEMBER_ID_ATTRIBUTE) as? String
    return resolvedMemberId ?: AuthContext.GUEST_MEMBER_ID
}
