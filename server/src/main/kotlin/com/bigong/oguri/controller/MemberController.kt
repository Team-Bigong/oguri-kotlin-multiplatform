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
     * 내 정보 조회 API (최초 진입 시 호출하여 닉네임 생성 등 수행)
     */
    @GetMapping("/me")
    fun getMyInfo(
        @RequestHeader(value = "X-USER-ID", defaultValue = "GUEST") memberId: String
    ): com.bigong.oguri.dto.MemberMeResponse {
        return memberService.getMyInfo(memberId)
    }

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
