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
import com.bigong.oguri.util.AppleIdentityTokenVerifier
import com.bigong.oguri.util.JwtTokenProvider
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException
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
    private val appleIdentityTokenVerifier: AppleIdentityTokenVerifier,
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
        val member = findOrCreateMember(kakaoId)
        return issueLoginTokens(member)
    }

    /**
     * Apple 로그인 및 가입
     */
    fun loginWithApple(identityToken: String): LoginResponse {
        val appleSubject = appleIdentityTokenVerifier.extractAppleSubject(identityToken)
        val appleMemberId = "APPLE_$appleSubject"
        val member = findOrCreateMember(appleMemberId)
        return issueLoginTokens(member)
    }

    /**
     * 내 정보 및 저장된 데이터 전체 조회
     */
    fun getMyInfo(memberId: String): MemberMeResponse {
        val member = memberRepository.findById(memberId).orElseGet {
            memberRepository.save(Member(id = memberId).apply { setNicknameOnce(generateUniqueNickname()) })
        }

        val savedPeriods = savedRecommendationRepository.findAllByMemberId(memberId).map {
            SavedPeriodDto(
                startDate = it.startDate,
                endDate = it.endDate,
                dayOffCount = it.dayOffCount,
                totalTripCount = it.totalTripCount
            )
        }

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
            onboardingCompleted = member.onboardingCompleted,
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

    private fun getKakaoUserInfo(accessToken: String): KakaoUserInfoResponse {
        val url = "https://kapi.kakao.com/v2/user/me"
        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $accessToken")
            set("Content-type", "application/x-www-form-urlencoded;charset=utf-8")
        }
        return restTemplate.exchange(url, HttpMethod.GET, HttpEntity<Any>(headers), KakaoUserInfoResponse::class.java).body
            ?: throw RuntimeException("카카오 통신 실패")
    }

    private fun findOrCreateMember(memberId: String): Member {
        return memberRepository.findById(memberId).orElseGet {
            val newMember = Member(id = memberId)
            newMember.setNicknameOnce(generateUniqueNickname())
            memberRepository.save(newMember)
        }
    }

    private fun issueLoginTokens(member: Member): LoginResponse {
        val serviceAccessToken = jwtTokenProvider.createAccessToken(member.id)
        val serviceRefreshToken = jwtTokenProvider.createRefreshToken(member.id)
        member.updateRefreshToken(serviceRefreshToken)
        memberRepository.save(member)
        return LoginResponse(
            accessToken = serviceAccessToken,
            refreshToken = serviceRefreshToken,
            nickname = member.nickname ?: "",
            onboardingCompleted = member.onboardingCompleted
        )
    }

    fun updateDayOffInfo(memberId: String, preferred: Int, remaining: Int) {
        validateDayOffRules(preferred, remaining)
        val member = memberRepository.findById(memberId).orElseGet {
            memberRepository.save(Member(id = memberId).apply { setNicknameOnce(generateUniqueNickname()) })
        }
        member.updateDayOffInfo(preferred, remaining)
        memberRepository.save(member)
    }

    fun completeOnboarding(memberId: String, preferred: Int, remaining: Int) {
        validateDayOffRules(preferred, remaining)
        val member = memberRepository.findById(memberId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.")
        }
        member.completeOnboarding(preferred, remaining)
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

    private fun validateDayOffRules(preferredDayOff: Int, remainingDayOff: Int) {
        if (remainingDayOff < MINIMUM_DAY_OFF) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "남은 연차는 0일 이상이어야 합니다.")
        }
        if (remainingDayOff > MAXIMUM_REMAINING_DAY_OFF) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "남은 연차는 최대 40일까지 입력할 수 있습니다.")
        }
        if (preferredDayOff < MINIMUM_DAY_OFF) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "선호 연차는 0일 이상이어야 합니다.")
        }
        if (preferredDayOff > remainingDayOff) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "선호 연차는 남은 연차를 초과할 수 없습니다.")
        }
    }

    /**
     * 유니크한 랜덤 닉네임 생성 로직 (형식: 형용사 명사_숫자5자리)
     */
    private fun generateUniqueNickname(): String {
        val adjectives = adjectiveRepository.findAll()
        val nouns = nounRepository.findAll()

        // 단어가 하나도 없을 경우 대비 (안전장치)
        if (adjectives.isEmpty() || nouns.isEmpty()) {
            return "여행자_" + String.format("%05d", Random.nextInt(100000))
        }

        var nickname: String
        do {
            val adj = adjectives.random().word
            val noun = nouns.random().word
            val number = String.format("%05d", Random.nextInt(100000))
            nickname = "$adj ${noun}_$number" // 최종 형식 적용: 형용사[공백]명사_숫자
        } while (memberRepository.existsByNickname(nickname))

        return nickname
    }

    private companion object {
        private const val MINIMUM_DAY_OFF: Int = 0
        private const val MAXIMUM_REMAINING_DAY_OFF: Int = 40
    }
}
