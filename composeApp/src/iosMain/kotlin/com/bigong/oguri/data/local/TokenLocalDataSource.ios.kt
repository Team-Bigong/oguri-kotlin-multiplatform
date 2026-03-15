package com.bigong.oguri.data.local

import platform.Foundation.NSUserDefaults

private const val KEY_ACCESS_TOKEN: String = "key_access_token"
private const val KEY_REFRESH_TOKEN: String = "key_refresh_token"

private object IosTokenLocalDataSource : TokenLocalDataSource {
    private val userDefaults: NSUserDefaults = NSUserDefaults.standardUserDefaults

    override fun initialize() = Unit

    override fun readAccessToken(): String? {
        return userDefaults.stringForKey(KEY_ACCESS_TOKEN)
    }

    override fun readRefreshToken(): String? {
        return userDefaults.stringForKey(KEY_REFRESH_TOKEN)
    }

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
