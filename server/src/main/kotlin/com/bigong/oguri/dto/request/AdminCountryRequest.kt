package com.bigong.oguri.dto.request

import java.math.BigDecimal

data class AdminCountryUpsertRequest(
    val name: String,
    val currencyCode: String? = null,
    val bigMacIndex: BigDecimal? = null,
)
