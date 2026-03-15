package com.bigong.oguri.controller

import com.bigong.oguri.dto.CalendarResponse
import com.bigong.oguri.service.CalendarService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.YearMonth

@Tag(name = "Calendar API", description = "캘린더 화면 관련 API")
@RestController
@RequestMapping("/api/v1/calendar")
class CalendarController(
    private val calendarService: CalendarService
) {
    @Operation(summary = "캘린더 조회", description = "특정 년월의 최적 연휴 구간과 공휴일 목록을 반환합니다.")
    @GetMapping
    fun getCalendar(
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM") yearMonth: YearMonth,
        @RequestParam(required = false) dayOffCount: Int?,
        @RequestHeader(value = "X-USER-ID", defaultValue = "GUEST") memberId: String
    ): CalendarResponse {
        return calendarService.getCalendarData(yearMonth, memberId, dayOffCount)
    }
}
