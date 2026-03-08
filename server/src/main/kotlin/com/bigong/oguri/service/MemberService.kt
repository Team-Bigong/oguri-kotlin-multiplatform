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
     * 내 정보 조회 (닉네임이 없으면 랜덤 생성)
     */
    fun getMyInfo(memberId: String): MemberMeResponse {
        val member = memberRepository.findById(memberId).orElseGet {
            memberRepository.save(Member(id = memberId))
        }

        // 닉네임이 없으면 생성
        if (member.nickname == null) {
            val generated = generateUniqueNickname()
            member.setNicknameOnce(generated)
            memberRepository.save(member)
        }

        return MemberMeResponse(
            id = member.id,
            nickname = member.nickname!!,
            dayOffCount = member.dayOffCount
        )
    }

    /**
     * 연차 개수 업데이트
     */
    fun updateDayOffCount(memberId: String, dayOffCount: Int): Int {
        val member = memberRepository.findById(memberId).orElseGet {
            memberRepository.save(Member(id = memberId))
        }
        member.updateDayOffCount(dayOffCount)
        return memberRepository.save(member).dayOffCount
    }

    /**
     * 연차 개수 조회
     */
    @Transactional(readOnly = true)
    fun getDayOffCount(memberId: String): Int {
        return memberRepository.findById(memberId)
            .map { it.dayOffCount }
            .orElse(3)
    }

    /**
     * 유니크한 랜덤 닉네임 생성 로직
     */
    private fun generateUniqueNickname(): String {
        val adjectives = adjectiveRepository.findAll()
        val nouns = nounRepository.findAll()

        // 단어가 하나도 없을 경우 대비 (안전장치)
        if (adjectives.isEmpty() || nouns.isEmpty()) {
            return "여행자 " + String.format("%05d", Random.nextInt(100000))
        }

        var nickname: String
        do {
            val adj = adjectives.random().word
            val noun = nouns.random().word
            val number = String.format("%05d", Random.nextInt(100000))
            nickname = "$adj ${noun}_$number"
        } while (memberRepository.existsByNickname(nickname)) // 중복 체크

        return nickname
    }
}
