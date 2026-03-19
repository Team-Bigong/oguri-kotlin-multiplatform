package com.bigong.oguri.controller

import com.bigong.oguri.dto.AdminMemberCreateRequest
import com.bigong.oguri.dto.AdminMemberResponse
import com.bigong.oguri.dto.AdminMemberUpdateRequest
import com.bigong.oguri.service.AdminMemberService
import io.swagger.v3.oas.annotations.Hidden
import org.springframework.web.bind.annotation.*

@Hidden
@RestController
@RequestMapping("/api/admin/v1/members")
class AdminMemberController(
    private val adminMemberService: AdminMemberService
) {
    @GetMapping
    fun getMemberList(): List<AdminMemberResponse> {
        return adminMemberService.getMemberList()
    }

    @PostMapping
    fun createMember(
        @RequestBody request: AdminMemberCreateRequest
    ): AdminMemberResponse {
        return adminMemberService.createMember(request)
    }

    @PutMapping("/{memberId}")
    fun updateMember(
        @PathVariable memberId: String,
        @RequestBody request: AdminMemberUpdateRequest
    ): AdminMemberResponse {
        return adminMemberService.updateMember(memberId, request)
    }

    @DeleteMapping("/{memberId}")
    fun deleteMember(
        @PathVariable memberId: String
    ) {
        adminMemberService.deleteMember(memberId)
    }
}
