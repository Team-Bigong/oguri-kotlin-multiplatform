package com.bigong.oguri.repository

import com.bigong.oguri.domain.Recommendation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RecommendationRepository : JpaRepository<Recommendation, Int>
