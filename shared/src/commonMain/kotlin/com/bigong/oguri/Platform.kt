package com.bigong.oguri

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform