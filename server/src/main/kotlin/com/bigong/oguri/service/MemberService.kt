package com.bigong.oguri.service

import com.bigong.oguri.domain.Member
import com.bigong.oguri.dto.MemberMeResponse
import com.bigong.oguri.repository.AdjectiveRepository
import com.bigong.oguri.repository.MemberRepository
import com.bigong.oguri.repository.NounRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.random.Random

@Service
@Transactional
class MemberService(
    private val memberRepository: MemberRepository,
    private val adjectiveRepository: AdjectiveRepository,
    private val nounRepository: NounRepository
) {
    /**
     * 내 정보 조회 (닉네임 자동 생성 포함)
     */
    fun getMyInfo(memberId: String): MemberMeResponse {
        val member = memberRepository.findById(memberId).orElseGet {
            memberRepository.save(Member(id = memberId))
        }

        if (member.nickname == null) {
            val generated = generateUniqueNickname()
            member.setNicknameOnce(generated)
            memberRepository.save(member)
        }

        return MemberMeResponse(
            id = member.id,
            nickname = member.nickname!!,
            preferredDayOff = member.preferredDayOff,
            remainingDayOff = member.remainingDayOff
        )
    }

    /**
     * 연차 정보 업데이트 (선호/잔여)
     */
    fun updateDayOffInfo(memberId: String, preferred: Int, remaining: Int) {
        val member = memberRepository.findById(memberId).orElseGet {
            memberRepository.save(Member(id = memberId))
        }
        member.updateDayOffInfo(preferred, remaining)
        memberRepository.save(member)
    }

    /**
     * 멤버의 연차 정보(선호/잔여) 전체 조회
     */
    @Transactional(readOnly = true)
    fun getDayOffInfo(memberId: String): com.bigong.oguri.dto.MemberDayOffResponse {
        val member = memberRepository.findById(memberId).orElseGet {
            Member(id = memberId)
        }
        return com.bigong.oguri.dto.MemberDayOffResponse(
            preferredDayOff = member.preferredDayOff,
            remainingDayOff = member.remainingDayOff
        )
    }

    /**
     * 멤버의 선호 연차 개수 조회
     */
    @Transactional(readOnly = true)
    fun getPreferredDayOff(memberId: String): Int {
        return memberRepository.findById(memberId)
            .map { it.preferredDayOff }
            .orElse(3)
    }

    /**
     * 멤버의 잔여 연차 개수 조회
     */
    @Transactional(readOnly = true)
    fun getRemainingDayOff(memberId: String): Int {
        return memberRepository.findById(memberId)
            .map { it.remainingDayOff }
            .orElse(3)
    }

    private fun generateUniqueNickname(): String {
        val adjectives = adjectiveRepository.findAll()
        val nouns = nounRepository.findAll()

        if (adjectives.isEmpty() || nouns.isEmpty()) {
            return "여행자 " + String.format("%05d", Random.nextInt(100000))
        }

        var nickname: String
        do {
            val adj = adjectives.random().word
            val noun = nouns.random().word
            val number = String.format("%05d", Random.nextInt(100000))
            nickname = "$adj $noun $number"
        } while (memberRepository.existsByNickname(nickname))

        return nickname
    }
}
