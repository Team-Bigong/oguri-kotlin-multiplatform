package com.bigong.oguri.controller

import com.bigong.oguri.dto.CalendarResponse
import com.bigong.oguri.service.CalendarService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.YearMonth

@RestController
@RequestMapping("/api/v1/calendar")
class CalendarController(
    private val calendarService: CalendarService
) {
    /**
     * 캘린더 화면 데이터 조회 API
     * @param yearMonth 조회할 년월 (형식: yyyy-MM, 예: 2026-03)
     */
    @GetMapping
    fun getCalendar(
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM") yearMonth: YearMonth,
        @RequestParam(required = false) dayOffCount: Int?,
        @RequestHeader(value = "X-USER-ID", defaultValue = "GUEST") memberId: String
    ): CalendarResponse {
        return calendarService.getCalendarData(yearMonth, memberId, dayOffCount)
    }
}
