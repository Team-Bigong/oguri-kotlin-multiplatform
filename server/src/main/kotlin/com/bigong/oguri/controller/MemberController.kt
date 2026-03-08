package com.bigong.oguri.controller

import com.bigong.oguri.dto.SaveRecommendationRequest
import com.bigong.oguri.dto.UserSettingRequest
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
     * 멤버 연차 개수 설정 저장 API
     */
    @PostMapping("/day-off")
    fun updateDayOffCount(
        @RequestBody request: UserSettingRequest,
        @RequestHeader(value = "X-USER-ID", defaultValue = "GUEST") memberId: String
    ): Int {
        return memberService.updateDayOffCount(memberId, request.dayOffCount)
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
