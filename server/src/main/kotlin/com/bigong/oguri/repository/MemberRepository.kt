package com.bigong.oguri.repository

import com.bigong.oguri.domain.Member
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MemberRepository : JpaRepository<Member, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select member from Member member where member.id = :memberId")
    fun findForTravelCreation(memberId: String): Member?

    fun existsByNickname(nickname: String): Boolean

    fun findAllByOrderByUpdatedAtDesc(): List<Member>
}
