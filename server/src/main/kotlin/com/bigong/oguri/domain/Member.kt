package com.bigong.oguri.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "members")
class Member(
    @Id
    @Column(name = "id", length = 100)
    val id: String,

    @Column(unique = true, length = 100)
    var nickname: String? = null,

    @Column(columnDefinition = "TEXT")
    var refreshToken: String? = null,

    @Column(nullable = false)
    var preferredDayOff: Int = 3,

    @Column(nullable = false)
    var remainingDayOff: Int = 3,

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun updateDayOffInfo(preferred: Int, remaining: Int) {
        this.preferredDayOff = preferred
        this.remainingDayOff = remaining
        this.updatedAt = LocalDateTime.now()
    }

    fun setNicknameOnce(generatedNickname: String) {
        if (this.nickname == null) {
            this.nickname = generatedNickname
            this.updatedAt = LocalDateTime.now()
        }
    }

    fun updateRefreshToken(newToken: String) {
        this.refreshToken = newToken
        this.updatedAt = LocalDateTime.now()
    }
}
