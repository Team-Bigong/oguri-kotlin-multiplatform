package com.bigong.oguri.data.local

interface TokenLocalDataSource {
    fun initialize()

    fun readAccessToken(): String?

    fun readRefreshToken(): String?

    fun writeTokens(
        accessToken: String,
        refreshToken: String,
    )

    fun clearTokens()
}
