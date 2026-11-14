package com.bigong.oguri.data.local

interface TravelRecoveryStorage {
    fun <Result> access(block: (String?, (String?) -> Unit) -> Result): Result
}

expect fun travelRecoveryStorage(): TravelRecoveryStorage
