package com.bigong.oguri.service

import com.bigong.oguri.domain.Destination
import com.bigong.oguri.domain.DestinationImage
import com.bigong.oguri.dto.AdminCountryResponse
import com.bigong.oguri.dto.AdminDestinationResponse
import com.bigong.oguri.dto.AdminDestinationUpsertRequest
import com.bigong.oguri.dto.AdminDestinationImageResponse
import com.bigong.oguri.repository.CountryRepository
import com.bigong.oguri.repository.DestinationImageRepository
import com.bigong.oguri.repository.DestinationRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class AdminDestinationService(
    private val destinationRepository: DestinationRepository,
    private val destinationImageRepository: DestinationImageRepository,
    private val countryRepository: CountryRepository
) {
    @Transactional(readOnly = true)
    fun getCountryList(): List<AdminCountryResponse> {
        return countryRepository.findAllByOrderByNameAsc().map { country ->
            AdminCountryResponse(
                id = country.id,
                name = country.name
            )
        }
    }

    @Transactional(readOnly = true)
    fun getDestinationList(): List<AdminDestinationResponse> {
        return destinationRepository.findAllWithCountryAndImages().map { destination ->
            destination.toAdminResponse()
        }
    }

    fun createDestination(request: AdminDestinationUpsertRequest): AdminDestinationResponse {
        validateDestinationRequest(request)

        val country = countryRepository.findById(request.countryId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "국가를 찾을 수 없습니다. id=${request.countryId}")
        }

        val destination = destinationRepository.save(
            Destination(
                country = country,
                name = request.name.trim(),
                summary = normalizeNullableText(request.summary),
                description = normalizeNullableText(request.description),
                recommendStartMonth1 = request.recommendStartMonth1,
                recommendEndMonth1 = request.recommendEndMonth1,
                recommendStartMonth2 = request.recommendStartMonth2,
                recommendEndMonth2 = request.recommendEndMonth2,
                flightTime = null,
                flightTimeMinutes = request.flightTimeMinutes
            )
        )

        replaceDestinationImages(destination, request)

        return destinationRepository.findByIdWithCountryAndImages(destination.id)
            ?.toAdminResponse()
            ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "여행지 생성 후 조회에 실패했습니다.")
    }

    fun updateDestination(destinationId: Int, request: AdminDestinationUpsertRequest): AdminDestinationResponse {
        validateDestinationRequest(request)

        val destination = destinationRepository.findById(destinationId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "여행지를 찾을 수 없습니다. id=$destinationId")
        }

        val country = countryRepository.findById(request.countryId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "국가를 찾을 수 없습니다. id=${request.countryId}")
        }

        destination.country = country
        destination.name = request.name.trim()
        destination.summary = normalizeNullableText(request.summary)
        destination.description = normalizeNullableText(request.description)
        destination.recommendStartMonth1 = request.recommendStartMonth1
        destination.recommendEndMonth1 = request.recommendEndMonth1
        destination.recommendStartMonth2 = request.recommendStartMonth2
        destination.recommendEndMonth2 = request.recommendEndMonth2
        destination.flightTime = null
        destination.flightTimeMinutes = request.flightTimeMinutes

        destinationRepository.save(destination)
        replaceDestinationImages(destination, request)

        return destinationRepository.findByIdWithCountryAndImages(destination.id)
            ?.toAdminResponse()
            ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "여행지 수정 후 조회에 실패했습니다.")
    }

    fun deleteDestination(destinationId: Int) {
        if (!destinationRepository.existsById(destinationId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "여행지를 찾을 수 없습니다. id=$destinationId")
        }
        destinationRepository.deleteById(destinationId)
    }

    private fun replaceDestinationImages(destination: Destination, request: AdminDestinationUpsertRequest) {
        destinationImageRepository.deleteAllByDestinationId(destination.id)

        request.images
            .sortedBy { image -> image.sortOrder }
            .forEach { image ->
                destinationImageRepository.save(
                    DestinationImage(
                        destination = destination,
                        imageUrl = image.imageUrl.trim(),
                        isThumbnail = image.isThumbnail,
                        sortOrder = image.sortOrder
                    )
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
            "추천 기간 1"
        )
        validateMonthRange(
            request.recommendStartMonth2,
            request.recommendEndMonth2,
            "추천 기간 2"
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
    }

    private fun validateMonthRange(startMonth: Int?, endMonth: Int?, label: String) {
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

    private fun normalizeNullableText(value: String?): String? {
        val trimmedValue = value?.trim() ?: return null
        return if (trimmedValue.isBlank()) null else trimmedValue
    }

    private fun Destination.toAdminResponse(): AdminDestinationResponse {
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
            flightTimeMinutes = flightTimeMinutes ?: parseLegacyFlightTimeMinutes(flightTime),
            images = sortedImages.map { image ->
                AdminDestinationImageResponse(
                    id = image.id,
                    imageUrl = image.imageUrl,
                    isThumbnail = image.isThumbnail,
                    sortOrder = image.sortOrder
                )
            }
        )
    }

    private companion object {
        private const val MINIMUM_MONTH = 1
        private const val MAXIMUM_MONTH = 12
        private const val EXACT_THUMBNAIL_COUNT = 1
        private const val MINIMUM_IMAGE_SORT_ORDER = 1
        private const val MINIMUM_FLIGHT_TIME_MINUTES = 0
        private val NUMBER_ONLY_REGEX = Regex("[^0-9]")
    }

    private fun parseLegacyFlightTimeMinutes(legacyFlightTime: String?): Int? {
        if (legacyFlightTime == null) {
            return null
        }
        val number = legacyFlightTime.replace(NUMBER_ONLY_REGEX, "")
        return number.toIntOrNull()
    }
}
