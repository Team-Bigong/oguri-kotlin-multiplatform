package com.bigong.oguri.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "search_logs")
class SearchLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    @Column(name = "member_id", nullable = false)
    val memberId: String,
    @Column(name = "query", nullable = false)
    val query: String,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
