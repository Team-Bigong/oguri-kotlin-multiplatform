package com.bigong.oguri.service

import com.bigong.oguri.domain.Member
import com.bigong.oguri.dto.KakaoUserInfoResponse
import com.bigong.oguri.dto.LoginResponse
import com.bigong.oguri.dto.MemberMeResponse
import com.bigong.oguri.repository.AdjectiveRepository
import com.bigong.oguri.repository.MemberRepository
import com.bigong.oguri.repository.NounRepository
import com.bigong.oguri.util.JwtTokenProvider
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import kotlin.random.Random

@Service
@Transactional
class MemberService(
    private val memberRepository: MemberRepository,
    private val adjectiveRepository: AdjectiveRepository,
    private val nounRepository: NounRepository,
    private val jwtTokenProvider: JwtTokenProvider
) {
    private val restTemplate = RestTemplate()

    /**
     * 카카오 로그인을 통한 토큰 발급 및 가입
     */
    fun loginWithKakao(accessToken: String): LoginResponse {
        val kakaoUserInfo = getKakaoUserInfo(accessToken)
        val kakaoId = "KAKAO_${kakaoUserInfo.id}" // 카카오 유저임을 명시

        val member = memberRepository.findById(kakaoId).orElseGet {
            val newMember = Member(id = kakaoId)
            val generatedNickname = generateUniqueNickname()
            newMember.setNicknameOnce(generatedNickname)
            memberRepository.save(newMember)
        }

        // 우리 서비스 전용 토큰 생성
        val serviceAccessToken = jwtTokenProvider.createAccessToken(member.id)
        val serviceRefreshToken = jwtTokenProvider.createRefreshToken(member.id)

        // 리프레시 토큰 DB 업데이트
        member.updateRefreshToken(serviceRefreshToken)
        memberRepository.save(member)

        return LoginResponse(
            accessToken = serviceAccessToken,
            refreshToken = serviceRefreshToken,
            nickname = member.nickname ?: ""
        )
    }

    private fun getKakaoUserInfo(accessToken: String): KakaoUserInfoResponse {
        val url = "https://kapi.kakao.com/v2/user/me"
        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer $accessToken")
        headers.set("Content-type", "application/x-www-form-urlencoded;charset=utf-8")

        val entity = HttpEntity<Any>(headers)
        val response = restTemplate.exchange(url, HttpMethod.GET, entity, KakaoUserInfoResponse::class.java)

        return response.body ?: throw RuntimeException("카카오 유저 정보를 가져오는데 실패했습니다.")
    }

    /**
     * 내 정보 조회
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

    fun updateDayOffInfo(memberId: String, preferred: Int, remaining: Int) {
        val member = memberRepository.findById(memberId).orElseGet {
            memberRepository.save(Member(id = memberId))
        }
        member.updateDayOffInfo(preferred, remaining)
        memberRepository.save(member)
    }

    @Transactional(readOnly = true)
    fun getRemainingDayOff(memberId: String): Int {
        return memberRepository.findById(memberId).map { it.remainingDayOff }.orElse(3)
    }

    @Transactional(readOnly = true)
    fun getPreferredDayOff(memberId: String): Int {
        return memberRepository.findById(memberId).map { it.preferredDayOff }.orElse(3)
    }

    @Transactional(readOnly = true)
    fun getDayOffInfo(memberId: String): com.bigong.oguri.dto.MemberDayOffResponse {
        val member = memberRepository.findById(memberId).orElseGet { Member(id = memberId) }
        return com.bigong.oguri.dto.MemberDayOffResponse(
            preferredDayOff = member.preferredDayOff,
            remainingDayOff = member.remainingDayOff
        )
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
