package com.bigong.oguri.service

import com.bigong.oguri.domain.Member
import com.bigong.oguri.dto.AdminMemberCreateRequest
import com.bigong.oguri.dto.AdminMemberResponse
import com.bigong.oguri.dto.AdminMemberUpdateRequest
import com.bigong.oguri.repository.MemberRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class AdminMemberService(
    private val memberRepository: MemberRepository,
) {
    @Transactional(readOnly = true)
    fun getMemberList(): List<AdminMemberResponse> =
        memberRepository.findAllByOrderByUpdatedAtDesc().map { member ->
            member.toAdminResponse()
        }

    fun createMember(request: AdminMemberCreateRequest): AdminMemberResponse {
        if (request.id.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "회원 ID는 필수입니다.")
        }
        if (memberRepository.existsById(request.id)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 회원 ID입니다.")
        }

        validateDayOff(request.preferredDayOff, request.remainingDayOff)

        val member =
            memberRepository.save(
                Member(
                    id = request.id.trim(),
                    nickname = normalizeNullableText(request.nickname),
                    preferredDayOff = request.preferredDayOff,
                    remainingDayOff = request.remainingDayOff,
                    onboardingCompleted = request.onboardingCompleted,
                ),
            )

        return member.toAdminResponse()
    }

    fun updateMember(
        memberId: String,
        request: AdminMemberUpdateRequest,
    ): AdminMemberResponse {
        validateDayOff(request.preferredDayOff, request.remainingDayOff)

        val member =
            memberRepository.findById(memberId).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다. id=$memberId")
            }

        member.nickname = normalizeNullableText(request.nickname)
        member.completeOnboarding(request.preferredDayOff, request.remainingDayOff)
        if (!request.onboardingCompleted) {
            member.onboardingCompleted = false
        }

        return memberRepository.save(member).toAdminResponse()
    }

    fun deleteMember(memberId: String) {
        if (!memberRepository.existsById(memberId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다. id=$memberId")
        }
        memberRepository.deleteById(memberId)
    }

    private fun validateDayOff(
        preferredDayOff: Int,
        remainingDayOff: Int,
    ) {
        if (remainingDayOff < MINIMUM_DAY_OFF) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "남은 연차는 0 이상이어야 합니다.")
        }
        if (remainingDayOff > MAXIMUM_DAY_OFF) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "남은 연차는 최대 40입니다.")
        }
        if (preferredDayOff < MINIMUM_DAY_OFF) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "선호 연차는 0 이상이어야 합니다.")
        }
        if (preferredDayOff > remainingDayOff) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "선호 연차는 남은 연차보다 클 수 없습니다.")
        }
    }

    private fun normalizeNullableText(value: String?): String? {
        val trimmedValue = value?.trim() ?: return null
        return if (trimmedValue.isBlank()) null else trimmedValue
    }

    private fun Member.toAdminResponse(): AdminMemberResponse =
        AdminMemberResponse(
            id = id,
            nickname = nickname,
            preferredDayOff = preferredDayOff,
            remainingDayOff = remainingDayOff,
            onboardingCompleted = onboardingCompleted,
            updatedAt = updatedAt.toString(),
        )

    private companion object {
        private const val MINIMUM_DAY_OFF = 0
        private const val MAXIMUM_DAY_OFF = 40
    }
}
