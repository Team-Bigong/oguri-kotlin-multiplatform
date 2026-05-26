package com.bigong.oguri.controller

import com.bigong.oguri.dto.request.AdminMemberCreateRequest
import com.bigong.oguri.dto.response.AdminMemberResponse
import com.bigong.oguri.dto.request.AdminMemberUpdateRequest
import com.bigong.oguri.service.AdminMemberService
import io.swagger.v3.oas.annotations.Hidden
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Hidden
@RestController
@RequestMapping("/api/admin/v1/members")
class AdminMemberController(
    private val adminMemberService: AdminMemberService,
) {
    @GetMapping
    fun getMemberList(): List<AdminMemberResponse> = adminMemberService.getMemberList()

    @PostMapping
    fun createMember(
        @RequestBody request: AdminMemberCreateRequest,
    ): AdminMemberResponse = adminMemberService.createMember(request)

    @PutMapping("/{memberId}")
    fun updateMember(
        @PathVariable memberId: String,
        @RequestBody request: AdminMemberUpdateRequest,
    ): AdminMemberResponse = adminMemberService.updateMember(memberId, request)

    @DeleteMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteMember(
        @PathVariable memberId: String,
    ) {
        adminMemberService.deleteMember(memberId)
    }
}
