package com.bigong.oguri.controller

import com.bigong.oguri.dto.MemberDayOffRequest
import com.bigong.oguri.dto.SaveRecommendationRequest
import com.bigong.oguri.service.MemberService
import com.bigong.oguri.service.SavedRecommendationService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/members")
class MemberController(
    private val memberService: MemberService,
    private val savedRecommendationService: SavedRecommendationService
) {
    /**
     * 내 정보 조회 API
     */
    @GetMapping("/me")
    fun getMyInfo(
        @RequestHeader(value = "X-USER-ID", defaultValue = "GUEST") memberId: String
    ): com.bigong.oguri.dto.MemberMeResponse {
        return memberService.getMyInfo(memberId)
    }

    /**
     * 멤버 연차 정보 설정 조회 API
     */
    @GetMapping("/day-off")
    fun getDayOffInfo(
        @RequestHeader(value = "X-USER-ID", defaultValue = "GUEST") memberId: String
    ): com.bigong.oguri.dto.MemberDayOffResponse {
        return memberService.getDayOffInfo(memberId)
    }

    /**
     * 멤버 연차 정보 설정(선호/잔여) 저장 API
     */
    @PostMapping("/day-off")
    fun updateDayOffInfo(
        @RequestBody request: MemberDayOffRequest,
        @RequestHeader(value = "X-USER-ID", defaultValue = "GUEST") memberId: String
    ) {
        memberService.updateDayOffInfo(memberId, request.preferredDayOff, request.remainingDayOff)
    }

    /**
     * 연휴 저장 API
     */
    @PostMapping("/saved-recommendations")
    fun saveRecommendation(
        @RequestBody request: SaveRecommendationRequest,
        @RequestHeader(value = "X-USER-ID", defaultValue = "GUEST") memberId: String
    ) {
        savedRecommendationService.save(request, memberId)
    }

    /**
     * 연휴 저장 취소(삭제) API
     */
    @DeleteMapping("/saved-recommendations")
    fun deleteRecommendation(
        @RequestBody request: SaveRecommendationRequest,
        @RequestHeader(value = "X-USER-ID", defaultValue = "GUEST") memberId: String
    ) {
        savedRecommendationService.delete(request, memberId)
    }
}
