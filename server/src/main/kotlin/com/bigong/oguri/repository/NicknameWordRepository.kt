package com.bigong.oguri.repository

import com.bigong.oguri.domain.Adjective
import com.bigong.oguri.domain.Noun
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AdjectiveRepository : JpaRepository<Adjective, Int>

@Repository
interface NounRepository : JpaRepository<Noun, Int>
