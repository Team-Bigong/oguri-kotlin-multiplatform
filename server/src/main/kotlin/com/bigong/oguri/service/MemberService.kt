package com.bigong.oguri.service

import com.bigong.oguri.domain.Member
import com.bigong.oguri.dto.KakaoUserInfoResponse
import com.bigong.oguri.dto.LoginResponse
import com.bigong.oguri.dto.MemberDayOffResponse
import com.bigong.oguri.dto.MemberMeResponse
import com.bigong.oguri.dto.SavedPeriodDto
import com.bigong.oguri.dto.SavedPlaceDto
import com.bigong.oguri.dto.TokenRefreshResponse
import com.bigong.oguri.repository.AdjectiveRepository
import com.bigong.oguri.repository.DestinationRepository
import com.bigong.oguri.repository.MemberRepository
import com.bigong.oguri.repository.NounRepository
import com.bigong.oguri.repository.SavedDestinationRepository
import com.bigong.oguri.repository.SavedRecommendationRepository
import com.bigong.oguri.util.JwtTokenProvider
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import java.time.temporal.ChronoUnit
import kotlin.random.Random

/**
 * 멤버 정보 및 인증, 마이페이지 데이터를 담당하는 서비스
 */
@Service
@Transactional
class MemberService(
    private val memberRepository: MemberRepository,
    private val adjectiveRepository: AdjectiveRepository,
    private val nounRepository: NounRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val savedRecommendationRepository: SavedRecommendationRepository,
    private val savedDestinationRepository: SavedDestinationRepository,
    private val destinationRepository: DestinationRepository
) {
    private val restTemplate = RestTemplate()

    /**
     * 카카오 로그인 및 가입
     */
    fun loginWithKakao(accessToken: String): LoginResponse {
        val kakaoUserInfo = getKakaoUserInfo(accessToken)
        val kakaoId = "KAKAO_${kakaoUserInfo.id}"

        val member = memberRepository.findById(kakaoId).orElseGet {
            val newMember = Member(id = kakaoId)
            newMember.setNicknameOnce(generateUniqueNickname())
            memberRepository.save(newMember)
        }

        val serviceAccessToken = jwtTokenProvider.createAccessToken(member.id)
        val serviceRefreshToken = jwtTokenProvider.createRefreshToken(member.id)

        member.updateRefreshToken(serviceRefreshToken)
        memberRepository.save(member)

        return LoginResponse(
            accessToken = serviceAccessToken,
            refreshToken = serviceRefreshToken,
            nickname = member.nickname ?: ""
        )
    }

    /**
     * 내 정보 및 저장된 데이터(연휴, 여행지) 전체 조회
     */
    fun getMyInfo(memberId: String): MemberMeResponse {
        // 1. 기본 멤버 정보 조회
        val member = memberRepository.findById(memberId).orElseGet {
            memberRepository.save(Member(id = memberId).apply { setNicknameOnce(generateUniqueNickname()) })
        }

        // 2. 저장된 연휴 리스트 조회 및 가공
        val savedPeriods = savedRecommendationRepository.findAllByMemberId(memberId).map {
            SavedPeriodDto(
                startDate = it.startDate,
                endDate = it.endDate,
                dayOffCount = it.dayOffCount,
                totalTripCount = ChronoUnit.DAYS.between(it.startDate, it.endDate).toInt() + 1
            )
        }

        // 3. 저장된 여행지 리스트 조회 및 상세 정보 조립
        val savedDestIds = savedDestinationRepository.findAllByMemberId(memberId).map { it.destinationId }
        val savedPlaces = if (savedDestIds.isNotEmpty()) {
            destinationRepository.findAllWithCountryAndImages()
                .filter { savedDestIds.contains(it.id) }
                .map { dest ->
                    SavedPlaceDto(
                        id = dest.id.toLong(),
                        country = dest.country?.name ?: "Unknown",
                        city = dest.name,
                        thumbnailUrl = dest.images.find { it.isThumbnail }?.imageUrl ?: ""
                    )
                }
        } else emptyList()

        return MemberMeResponse(
            id = member.id,
            nickname = member.nickname ?: "",
            preferredDayOff = member.preferredDayOff,
            remainingDayOff = member.remainingDayOff,
            savedPeriods = savedPeriods,
            savedPlaces = savedPlaces
        )
    }

    /**
     * 토큰 재발급
     */
    fun refreshAccessToken(refreshToken: String): TokenRefreshResponse {
        if (!jwtTokenProvider.validateToken(refreshToken)) throw RuntimeException("토큰 만료")
        val memberId = jwtTokenProvider.getMemberId(refreshToken)
        val member = memberRepository.findById(memberId).orElseThrow { RuntimeException("유저 없음") }
        if (member.refreshToken != refreshToken) throw RuntimeException("토큰 불일치")

        val newAccess = jwtTokenProvider.createAccessToken(member.id)
        val newRefresh = jwtTokenProvider.createRefreshToken(member.id)
        member.updateRefreshToken(newRefresh)
        memberRepository.save(member)

        return TokenRefreshResponse(accessToken = newAccess, refreshToken = newRefresh)
    }

    // ... (기타 헬퍼 메서드 생략) ...

    private fun getKakaoUserInfo(accessToken: String): KakaoUserInfoResponse {
        val url = "https://kapi.kakao.com/v2/user/me"
        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $accessToken")
            set("Content-type", "application/x-www-form-urlencoded;charset=utf-8")
        }
        return restTemplate.exchange(url, HttpMethod.GET, HttpEntity<Any>(headers), KakaoUserInfoResponse::class.java).body
            ?: throw RuntimeException("카카오 통신 실패")
    }

    fun updateDayOffInfo(memberId: String, preferred: Int, remaining: Int) {
        val member = memberRepository.findById(memberId).orElseGet {
            memberRepository.save(Member(id = memberId).apply { setNicknameOnce(generateUniqueNickname()) })
        }
        member.updateDayOffInfo(preferred, remaining)
        memberRepository.save(member)
    }

    @Transactional(readOnly = true)
    fun getDayOffInfo(memberId: String): MemberDayOffResponse {
        val member = memberRepository.findById(memberId).orElseGet { Member(id = memberId) }
        return MemberDayOffResponse(member.preferredDayOff, member.remainingDayOff)
    }

    @Transactional(readOnly = true)
    fun getPreferredDayOff(memberId: String): Int = memberRepository.findById(memberId).map { it.preferredDayOff }.orElse(3)

    @Transactional(readOnly = true)
    fun getRemainingDayOff(memberId: String): Int = memberRepository.findById(memberId).map { it.remainingDayOff }.orElse(3)

    private fun generateUniqueNickname(): String {
        val adjectives = adjectiveRepository.findAll()
        val nouns = nounRepository.findAll()
        if (adjectives.isEmpty() || nouns.isEmpty()) return "여행자 " + String.format("%05d", Random.nextInt(100000))
        var nickname: String
        do {
            nickname = "${adjectives.random().word} ${nouns.random().word} ${String.format("%05d", Random.nextInt(100000))}"
        } while (memberRepository.existsByNickname(nickname))
        return nickname
    }
}
