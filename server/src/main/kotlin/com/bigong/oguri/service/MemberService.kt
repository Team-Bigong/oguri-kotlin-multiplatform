package com.bigong.oguri.service

import com.bigong.oguri.domain.Member
import com.bigong.oguri.repository.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MemberService(
    private val memberRepository: MemberRepository
) {
    /**
     * 멤버의 연차 개수 설정 업데이트 (없으면 생성)
     */
    fun updateDayOffCount(memberId: String, dayOffCount: Int): Int {
        val member = memberRepository.findById(memberId).orElse(
            Member(id = memberId)
        )
        member.updateDayOffCount(dayOffCount)
        return memberRepository.save(member).dayOffCount
    }

    /**
     * 멤버의 연차 개수 조회 (기본값 3)
     */
    @Transactional(readOnly = true)
    fun getDayOffCount(memberId: String): Int {
        return memberRepository.findById(memberId)
            .map { it.dayOffCount }
            .orElse(3)
    }
}
