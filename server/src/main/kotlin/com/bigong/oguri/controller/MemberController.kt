package com.bigong.oguri.controller

import com.bigong.oguri.dto.MemberDayOffRequest
import com.bigong.oguri.dto.MemberMeResponse
import com.bigong.oguri.dto.SaveRecommendationRequest
import com.bigong.oguri.service.MemberService
import com.bigong.oguri.service.SavedRecommendationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@Tag(name = "Member API", description = "사용자 설정 및 저장된 연휴 관리 API")
@RestController
@RequestMapping("/api/v1/members")
class MemberController(
    private val memberService: MemberService,
    private val savedRecommendationService: SavedRecommendationService
) {
    @Operation(summary = "내 정보 조회", description = "현재 로그인된 유저의 프로필 및 연차 설정 정보를 조회합니다.")
    @GetMapping("/me")
    fun getMyInfo(
        @RequestHeader(value = "X-USER-ID", defaultValue = "GUEST") memberId: String
    ): MemberMeResponse {
        return memberService.getMyInfo(memberId)
    }

    @Operation(summary = "연차 정보 수정", description = "사용자의 선호 연차 및 잔여 연차 정보를 업데이트합니다.")
    @PostMapping("/day-off")
    fun updateDayOffInfo(
        @RequestBody request: MemberDayOffRequest,
        @RequestHeader(value = "X-USER-ID", defaultValue = "GUEST") memberId: String
    ) {
        memberService.updateDayOffInfo(memberId, request.preferredDayOff, request.remainingDayOff)
    }

    @Operation(summary = "연휴 저장", description = "마음에 드는 연휴 구간을 내 목록에 저장합니다.")
    @PostMapping("/saved-recommendations")
    fun saveRecommendation(
        @RequestBody request: SaveRecommendationRequest,
        @RequestHeader(value = "X-USER-ID", defaultValue = "GUEST") memberId: String
    ) {
        savedRecommendationService.save(request, memberId)
    }

    @Operation(summary = "연휴 저장 취소", description = "저장했던 연휴 구간을 목록에서 삭제합니다.")
    @DeleteMapping("/saved-recommendations")
    fun deleteRecommendation(
        @RequestBody request: SaveRecommendationRequest,
        @RequestHeader(value = "X-USER-ID", defaultValue = "GUEST") memberId: String
    ) {
        savedRecommendationService.delete(request, memberId)
    }
}
