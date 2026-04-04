package com.bigong.oguri.controller

import com.bigong.oguri.config.resolveMemberId
import com.bigong.oguri.dto.CalendarResponse
import com.bigong.oguri.service.CalendarService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@Tag(name = "Calendar API", description = "캘린더 화면 관련 API")
@RestController
@RequestMapping("/api/v1/calendar")
class CalendarController(
    private val calendarService: CalendarService
) {
    @Operation(
        summary = "캘린더 추천 일정 조회",
        description = "가장 추천할만한 일정 순으로 정렬된 캘린더 추천 목록을 페이지 단위로 반환합니다. 각 일정에는 휴일 날짜 상세(holidayDateDetails)가 포함됩니다."
    )
    @GetMapping
    fun getCalendar(
        @Parameter(description = "조회 연도", example = "2026")
        @RequestParam year: Int,
        @Parameter(description = "조회 월(1~12, 미입력 시 해당 연도의 현재 월~12월 조회)", example = "3")
        @RequestParam(required = false) month: Int?,
        @Parameter(description = "연차 사용 일수(미입력 시 사용자 기본값 사용)", example = "3")
        @RequestParam(required = false) dayOffCount: Int?,
        @Parameter(description = "페이지 번호(0부터 시작)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기(1~50)", example = "10")
        @RequestParam(defaultValue = "10") size: Int,
        request: HttpServletRequest
    ): CalendarResponse {
        return calendarService.getCalendarData(
            year = year,
            month = month,
            memberId = request.resolveMemberId(),
            dayOffCount = dayOffCount,
            page = page,
            size = size
        )
    }

    @Operation(summary = "기간 상세 조회", description = "클릭한 특정 기간의 상세 정보와 추천 장소 목록을 페이지 단위로 반환합니다.")
    @GetMapping("/detail")
    fun getPeriodDetail(
        @Parameter(description = "일정 시작일", example = "2026-02-28")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @Parameter(description = "일정 종료일", example = "2026-03-04")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate,
        @Parameter(description = "사용자 국가", example = "대한민국")
        @RequestParam(defaultValue = "대한민국") userCountry: String,
        @Parameter(description = "페이지 번호(0부터 시작)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기(1~50)", example = "10")
        @RequestParam(defaultValue = "10") size: Int,
        request: HttpServletRequest
    ): com.bigong.oguri.dto.PeriodDetailResponse {
        return calendarService.getPeriodDetail(
            startDate = startDate,
            endDate = endDate,
            userCountry = userCountry,
            memberId = request.resolveMemberId(),
            page = page,
            size = size
        )
    }
}
