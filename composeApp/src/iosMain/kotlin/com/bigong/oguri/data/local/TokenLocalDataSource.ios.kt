package com.bigong.oguri.data.local

import platform.Foundation.NSUserDefaults

private const val KEY_ACCESS_TOKEN = "key_access_token"
private const val KEY_REFRESH_TOKEN = "key_refresh_token"

private object IosTokenLocalDataSource : TokenLocalDataSource {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    override fun initialize() = Unit

    override fun readAccessToken(): String? = userDefaults.stringForKey(KEY_ACCESS_TOKEN)

    override fun readRefreshToken(): String? = userDefaults.stringForKey(KEY_REFRESH_TOKEN)

    override fun writeTokens(
        accessToken: String,
        refreshToken: String,
    ) {
        userDefaults.setObject(accessToken, forKey = KEY_ACCESS_TOKEN)
        userDefaults.setObject(refreshToken, forKey = KEY_REFRESH_TOKEN)
    }

    override fun clearTokens() {
        userDefaults.removeObjectForKey(KEY_ACCESS_TOKEN)
        userDefaults.removeObjectForKey(KEY_REFRESH_TOKEN)
    }
}

actual fun provideTokenLocalDataSource(): TokenLocalDataSource = IosTokenLocalDataSource
