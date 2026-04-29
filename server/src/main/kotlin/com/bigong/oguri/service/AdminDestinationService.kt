package com.bigong.oguri.service

import com.bigong.oguri.domain.Country
import com.bigong.oguri.domain.Destination
import com.bigong.oguri.domain.DestinationExperience
import com.bigong.oguri.domain.DestinationImage
import com.bigong.oguri.dto.request.AdminCountryUpsertRequest
import com.bigong.oguri.dto.response.AdminCountryResponse
import com.bigong.oguri.dto.response.AdminDestinationExperienceResponse
import com.bigong.oguri.dto.response.AdminDestinationImageResponse
import com.bigong.oguri.dto.response.AdminDestinationResponse
import com.bigong.oguri.dto.request.AdminDestinationUpsertRequest
import com.bigong.oguri.repository.CountryRepository
import com.bigong.oguri.repository.DestinationExperienceRepository
import com.bigong.oguri.repository.DestinationImageRepository
import com.bigong.oguri.repository.DestinationRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal

@Service
@Transactional
class AdminDestinationService(
    private val destinationRepository: DestinationRepository,
    private val destinationImageRepository: DestinationImageRepository,
    private val destinationExperienceRepository: DestinationExperienceRepository,
    private val countryRepository: CountryRepository,
) {
    @Transactional(readOnly = true)
    fun getCountryList(): List<AdminCountryResponse> =
        countryRepository.findAllByOrderByNameAsc().map { country ->
            AdminCountryResponse(
                id = country.id,
                name = country.name,
                currencyCode = country.currencyCode,
                bigMacIndex = country.bigMacIndex,
            )
        }

    fun createCountry(request: AdminCountryUpsertRequest): AdminCountryResponse {
        validateCountryRequest(request)
        val trimmedCountryName = request.name.trim()
        if (countryRepository.existsByName(trimmedCountryName)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 국가 이름입니다: $trimmedCountryName")
        }
        val createdCountry = countryRepository.save(
            Country(
                name = trimmedCountryName,
                currencyCode = normalizeCurrencyCode(request.currencyCode),
                bigMacIndex = request.bigMacIndex,
            ),
        )
        return createdCountry.toAdminCountryResponse()
    }

    fun updateCountry(
        countryId: Int,
        request: AdminCountryUpsertRequest,
    ): AdminCountryResponse {
        validateCountryRequest(request)
        val trimmedCountryName = request.name.trim()
        if (countryRepository.existsByNameAndIdNot(trimmedCountryName, countryId)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 국가 이름입니다: $trimmedCountryName")
        }

        val country =
            countryRepository.findById(countryId).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "국가를 찾을 수 없습니다. id=$countryId")
            }
        country.name = trimmedCountryName
        country.currencyCode = normalizeCurrencyCode(request.currencyCode)
        country.bigMacIndex = request.bigMacIndex
        return countryRepository.save(country).toAdminCountryResponse()
    }

    fun deleteCountry(countryId: Int) {
        val country =
            countryRepository.findById(countryId).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "국가를 찾을 수 없습니다. id=$countryId")
            }

        val connectedDestinationCount = destinationRepository.findAllWithCountryAndImages().count { destination ->
            destination.country?.id == countryId
        }
        if (connectedDestinationCount > 0) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "연결된 여행지 ${connectedDestinationCount}건이 있어 국가를 삭제할 수 없습니다.",
            )
        }
        countryRepository.delete(country)
    }

    @Transactional(readOnly = true)
    fun getDestinationList(): List<AdminDestinationResponse> {
        val experiencesByDestinationId =
            destinationExperienceRepository
                .findAllByOrderByDestinationIdAscSortOrderAscIdAsc()
                .groupBy { experience -> experience.destinationId }
        return destinationRepository.findAllWithCountryAndImages().map { destination ->
            destination.toAdminResponse(
                experiences = experiencesByDestinationId[destination.id].orEmpty(),
            )
        }
    }

    fun createDestination(request: AdminDestinationUpsertRequest): AdminDestinationResponse {
        validateDestinationRequest(request)
        val destinationName = request.name.trim()
        validateDuplicateDestinationNameForCreate(destinationName)

        val country =
            countryRepository.findById(request.countryId).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "국가를 찾을 수 없습니다. id=${request.countryId}")
            }

        val destination =
            try {
                destinationRepository.save(
                    Destination(
                        country = country,
                        name = destinationName,
                        summary = normalizeNullableText(request.summary),
                        description = normalizeNullableText(request.description),
                        recommendStartMonth1 = request.recommendStartMonth1,
                        recommendEndMonth1 = request.recommendEndMonth1,
                        recommendStartMonth2 = request.recommendStartMonth2,
                        recommendEndMonth2 = request.recommendEndMonth2,
                        flightTimeMinutes = request.flightTimeMinutes,
                        flightUrl = normalizeNullableText(request.flightUrl),
                        weatherTemp1 = request.weatherTemp1,
                        weatherPrecipitationMm1 = request.weatherPrecipitationMm1,
                        weatherTemp2 = request.weatherTemp2,
                        weatherPrecipitationMm2 = request.weatherPrecipitationMm2,
                    ),
                )
            } catch (exception: DataIntegrityViolationException) {
                throw ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 여행지 이름입니다: $destinationName", exception)
            }

        replaceDestinationImages(destination, request)
        replaceDestinationExperiences(destination.id, request)

        return destinationRepository
            .findByIdWithCountryAndImages(destination.id)
            ?.toAdminResponse(
                images = destinationImageRepository.findAllByDestinationIdOrderBySortOrderAscIdAsc(destination.id),
                experiences = destinationExperienceRepository.findAllByDestinationIdOrderBySortOrderAscIdAsc(destination.id),
            )
            ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "여행지 생성 후 조회에 실패했습니다.")
    }

    fun updateDestination(
        destinationId: Int,
        request: AdminDestinationUpsertRequest,
    ): AdminDestinationResponse {
        validateDestinationRequest(request)
        val destinationName = request.name.trim()
        validateDuplicateDestinationNameForUpdate(destinationName, destinationId)

        val destination =
            destinationRepository.findById(destinationId).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "여행지를 찾을 수 없습니다. id=$destinationId")
            }

        val country =
            countryRepository.findById(request.countryId).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "국가를 찾을 수 없습니다. id=${request.countryId}")
            }

        destination.country = country
        destination.name = destinationName
        destination.summary = normalizeNullableText(request.summary)
        destination.description = normalizeNullableText(request.description)
        destination.recommendStartMonth1 = request.recommendStartMonth1
        destination.recommendEndMonth1 = request.recommendEndMonth1
        destination.recommendStartMonth2 = request.recommendStartMonth2
        destination.recommendEndMonth2 = request.recommendEndMonth2
        destination.flightTimeMinutes = request.flightTimeMinutes
        destination.flightUrl = normalizeNullableText(request.flightUrl)
        destination.weatherTemp1 = request.weatherTemp1
        destination.weatherPrecipitationMm1 = request.weatherPrecipitationMm1
        destination.weatherTemp2 = request.weatherTemp2
        destination.weatherPrecipitationMm2 = request.weatherPrecipitationMm2

        try {
            destinationRepository.save(destination)
        } catch (exception: DataIntegrityViolationException) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 여행지 이름입니다: $destinationName", exception)
        }
        replaceDestinationImages(destination, request)
        replaceDestinationExperiences(destination.id, request)

        return destinationRepository
            .findByIdWithCountryAndImages(destination.id)
            ?.toAdminResponse(
                images = destinationImageRepository.findAllByDestinationIdOrderBySortOrderAscIdAsc(destination.id),
                experiences = destinationExperienceRepository.findAllByDestinationIdOrderBySortOrderAscIdAsc(destination.id),
            )
            ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "여행지 수정 후 조회에 실패했습니다.")
    }

    fun deleteDestination(destinationId: Int) {
        if (!destinationRepository.existsById(destinationId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "여행지를 찾을 수 없습니다. id=$destinationId")
        }
        destinationRepository.deleteById(destinationId)
    }

    private fun replaceDestinationImages(
        destination: Destination,
        request: AdminDestinationUpsertRequest,
    ) {
        destinationImageRepository.deleteAllByDestinationId(destination.id)

        request.images
            .sortedBy { image -> image.sortOrder }
            .forEach { image ->
                destinationImageRepository.save(
                    DestinationImage(
                        destination = destination,
                        imageUrl = image.imageUrl.trim(),
                        isThumbnail = image.isThumbnail,
                        sortOrder = image.sortOrder,
                    ),
                )
            }
    }

    private fun replaceDestinationExperiences(
        destinationId: Int,
        request: AdminDestinationUpsertRequest,
    ) {
        destinationExperienceRepository.deleteAllByDestinationId(destinationId)

        request.experiences
            .sortedBy { experience -> experience.sortOrder }
            .forEach { experience ->
                destinationExperienceRepository.save(
                    DestinationExperience(
                        destinationId = destinationId,
                        title = experience.title.trim(),
                        description = experience.description.trim(),
                        thumbnailUrl = experience.thumbnailUrl.trim(),
                        link = experience.link.trim(),
                        sortOrder = experience.sortOrder,
                    ),
                )
            }
    }

    private fun validateDestinationRequest(request: AdminDestinationUpsertRequest) {
        if (request.name.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "여행지 이름은 필수입니다.")
        }

        validateMonthRange(
            request.recommendStartMonth1,
            request.recommendEndMonth1,
            "추천 기간 1",
        )
        validateMonthRange(
            request.recommendStartMonth2,
            request.recommendEndMonth2,
            "추천 기간 2",
        )

        val thumbnailCount = request.images.count { image -> image.isThumbnail }
        if (request.images.isNotEmpty() && thumbnailCount != EXACT_THUMBNAIL_COUNT) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지가 존재할 때 썸네일은 정확히 1개여야 합니다.")
        }

        if (request.images.any { image -> image.imageUrl.isBlank() }) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지 URL은 비어 있을 수 없습니다.")
        }

        if (request.images.any { image -> image.sortOrder < MINIMUM_IMAGE_SORT_ORDER }) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지 정렬 순서는 1 이상이어야 합니다.")
        }

        if (request.flightTimeMinutes != null && request.flightTimeMinutes < MINIMUM_FLIGHT_TIME_MINUTES) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "비행 시간(분)은 0 이상이어야 합니다.")
        }

        validateWeatherPeriod(request.weatherTemp1, request.weatherPrecipitationMm1, "날씨 정보 1")
        validateWeatherPeriod(request.weatherTemp2, request.weatherPrecipitationMm2, "날씨 정보 2")

        if (request.experiences.any { experience -> experience.title.isBlank() }) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "액티비티 제목은 비어 있을 수 없습니다.")
        }

        if (request.experiences.any { experience -> experience.description.isBlank() }) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "액티비티 설명은 비어 있을 수 없습니다.")
        }

        if (request.experiences.any { experience -> experience.thumbnailUrl.isBlank() }) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "액티비티 썸네일 URL은 비어 있을 수 없습니다.")
        }

        if (request.experiences.any { experience -> experience.link.isBlank() }) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "액티비티 링크는 비어 있을 수 없습니다.")
        }

        if (request.experiences.any { experience -> experience.sortOrder < MINIMUM_EXPERIENCE_SORT_ORDER }) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "액티비티 정렬 순서는 1 이상이어야 합니다.")
        }
    }

    private fun validateMonthRange(
        startMonth: Int?,
        endMonth: Int?,
        label: String,
    ) {
        if ((startMonth == null) != (endMonth == null)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "$label 시작/종료 월은 함께 입력해야 합니다.")
        }

        if (startMonth != null && (startMonth < MINIMUM_MONTH || startMonth > MAXIMUM_MONTH)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "$label 시작 월은 1~12 범위여야 합니다.")
        }

        if (endMonth != null && (endMonth < MINIMUM_MONTH || endMonth > MAXIMUM_MONTH)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "$label 종료 월은 1~12 범위여야 합니다.")
        }
    }

    private fun validateWeatherPeriod(
        temperature: Int?,
        precipitationMillimeter: Double?,
        label: String,
    ) {
        if ((temperature == null) != (precipitationMillimeter == null)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "$label 온도/강수량은 함께 입력해야 합니다.")
        }

        if (precipitationMillimeter != null && precipitationMillimeter < MINIMUM_PRECIPITATION_MILLIMETER) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "$label 강수량은 0 이상이어야 합니다.")
        }
    }

    private fun validateCountryRequest(request: AdminCountryUpsertRequest) {
        if (request.name.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "국가 이름은 필수입니다.")
        }

        val normalizedCurrencyCode = normalizeCurrencyCode(request.currencyCode)
        if (normalizedCurrencyCode != null && normalizedCurrencyCode.length != CURRENCY_CODE_LENGTH) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "통화 코드는 3자리여야 합니다.")
        }

        if (request.bigMacIndex != null && request.bigMacIndex < MINIMUM_BIG_MAC_INDEX) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "빅맥 지수는 0 이상이어야 합니다.")
        }
    }

    private fun normalizeNullableText(value: String?): String? {
        val trimmedValue = value?.trim() ?: return null
        return if (trimmedValue.isBlank()) null else trimmedValue
    }

    private fun normalizeCurrencyCode(value: String?): String? {
        val trimmedValue = value?.trim() ?: return null
        if (trimmedValue.isBlank()) {
            return null
        }
        return trimmedValue.uppercase()
    }

    private fun validateDuplicateDestinationNameForCreate(destinationName: String) {
        if (destinationRepository.existsByName(destinationName)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 여행지 이름입니다: $destinationName")
        }
    }

    private fun validateDuplicateDestinationNameForUpdate(
        destinationName: String,
        destinationId: Int,
    ) {
        if (destinationRepository.existsByNameAndIdNot(destinationName, destinationId)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 여행지 이름입니다: $destinationName")
        }
    }

    private fun Destination.toAdminResponse(
        images: List<DestinationImage> = this.images,
        experiences: List<DestinationExperience>,
    ): AdminDestinationResponse {
        val sortedImages = images.sortedBy { image -> image.sortOrder }
        return AdminDestinationResponse(
            id = id,
            countryId = country?.id,
            countryName = country?.name ?: "Unknown",
            name = name,
            summary = summary,
            description = description,
            recommendStartMonth1 = recommendStartMonth1,
            recommendEndMonth1 = recommendEndMonth1,
            recommendStartMonth2 = recommendStartMonth2,
            recommendEndMonth2 = recommendEndMonth2,
            flightTimeMinutes = flightTimeMinutes,
            flightUrl = flightUrl,
            weatherTemp1 = weatherTemp1,
            weatherPrecipitationMm1 = weatherPrecipitationMm1,
            weatherTemp2 = weatherTemp2,
            weatherPrecipitationMm2 = weatherPrecipitationMm2,
            images =
                sortedImages.map { image ->
                    AdminDestinationImageResponse(
                        id = image.id,
                        imageUrl = image.imageUrl,
                        isThumbnail = image.isThumbnail,
                        sortOrder = image.sortOrder,
                    )
                },
            experiences =
                experiences.map { experience ->
                    AdminDestinationExperienceResponse(
                        id = experience.id,
                        title = experience.title,
                        description = experience.description,
                        thumbnailUrl = experience.thumbnailUrl,
                        link = experience.link,
                        sortOrder = experience.sortOrder,
                    )
                },
        )
    }

    private fun Country.toAdminCountryResponse(): AdminCountryResponse {
        return AdminCountryResponse(
            id = id,
            name = name,
            currencyCode = currencyCode,
            bigMacIndex = bigMacIndex,
        )
    }

    private companion object {
        private const val MINIMUM_MONTH = 1
        private const val MAXIMUM_MONTH = 12
        private const val EXACT_THUMBNAIL_COUNT = 1
        private const val MINIMUM_IMAGE_SORT_ORDER = 1
        private const val MINIMUM_EXPERIENCE_SORT_ORDER = 1
        private const val MINIMUM_FLIGHT_TIME_MINUTES = 0
        private const val MINIMUM_PRECIPITATION_MILLIMETER = 0.0
        private const val CURRENCY_CODE_LENGTH = 3
        private val MINIMUM_BIG_MAC_INDEX = BigDecimal.ZERO
    }
}
