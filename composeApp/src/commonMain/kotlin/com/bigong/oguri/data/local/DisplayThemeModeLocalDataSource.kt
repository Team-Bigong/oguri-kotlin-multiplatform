package com.bigong.oguri.data.local

import com.bigong.oguri.domain.model.DisplayThemeMode

interface DisplayThemeModeLocalDataSource {
    fun initialize()

    fun readDisplayThemeMode(): DisplayThemeMode?

    fun writeDisplayThemeMode(displayThemeMode: DisplayThemeMode)
}
