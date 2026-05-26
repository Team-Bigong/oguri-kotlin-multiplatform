package com.bigong.oguri.service

import com.bigong.oguri.domain.PublicHoliday
import com.bigong.oguri.dto.response.AdminPublicHolidayResponse
import com.bigong.oguri.dto.request.AdminPublicHolidayUpsertRequest
import com.bigong.oguri.repository.PublicHolidayRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class AdminPublicHolidayService(
    private val publicHolidayRepository: PublicHolidayRepository,
) {
    @Transactional(readOnly = true)
    fun getPublicHolidayList(): List<AdminPublicHolidayResponse> =
        publicHolidayRepository.findAllByOrderByHolidayDateAsc().map { holiday ->
            holiday.toAdminResponse()
        }

    fun createPublicHoliday(request: AdminPublicHolidayUpsertRequest): AdminPublicHolidayResponse {
        if (request.name.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "공휴일 이름은 필수입니다.")
        }

        return publicHolidayRepository
            .save(
                PublicHoliday(
                    holidayDate = request.holidayDate,
                    name = request.name.trim(),
                    isActualHoliday = request.isActualHoliday,
                ),
            ).toAdminResponse()
    }

    fun updatePublicHoliday(
        holidayId: Int,
        request: AdminPublicHolidayUpsertRequest,
    ): AdminPublicHolidayResponse {
        if (request.name.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "공휴일 이름은 필수입니다.")
        }

        if (!publicHolidayRepository.existsById(holidayId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "공휴일을 찾을 수 없습니다. id=$holidayId")
        }

        return publicHolidayRepository
            .save(
                PublicHoliday(
                    id = holidayId,
                    holidayDate = request.holidayDate,
                    name = request.name.trim(),
                    isActualHoliday = request.isActualHoliday,
                ),
            ).toAdminResponse()
    }

    fun deletePublicHoliday(holidayId: Int) {
        if (!publicHolidayRepository.existsById(holidayId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "공휴일을 찾을 수 없습니다. id=$holidayId")
        }
        publicHolidayRepository.deleteById(holidayId)
    }

    private fun PublicHoliday.toAdminResponse(): AdminPublicHolidayResponse =
        AdminPublicHolidayResponse(
            id = id,
            holidayDate = holidayDate,
            name = name,
            isActualHoliday = isActualHoliday,
        )
}
