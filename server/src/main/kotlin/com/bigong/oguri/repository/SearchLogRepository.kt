package com.bigong.oguri.repository

import com.bigong.oguri.domain.SearchLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface SearchLogRepository : JpaRepository<SearchLog, Int> {

    @Query(
        value = """
            SELECT query 
            FROM search_logs 
            WHERE created_at >= :since 
            GROUP BY query 
            ORDER BY COUNT(id) DESC, MAX(created_at) DESC 
            LIMIT :limit
        """,
        nativeQuery = true,
    )
    fun findTrendingSearchTerms(
        since: LocalDateTime,
        limit: Int,
    ): List<String>
}
