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

    @Column(nullable = false)
    var dayOffCount: Int = 3,

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun updateDayOffCount(count: Int) {
        this.dayOffCount = count
        this.updatedAt = LocalDateTime.now()
    }

    fun setNicknameOnce(generatedNickname: String) {
        if (this.nickname == null) {
            this.nickname = generatedNickname
            this.updatedAt = LocalDateTime.now()
        }
    }
}
