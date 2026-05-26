package com.bigong.oguri.dto.response

/**
 * 검색 자동완성 개별 항목 응답 객체
 */
data class SearchAutocompleteItemResponse(
    val id: Long,
    val destinationName: String,
    val countryName: String,
)
