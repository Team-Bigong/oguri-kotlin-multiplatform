package com.bigong.oguri.domain

import jakarta.persistence.*

@Entity
@Table(name = "adjectives")
class Adjective(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(nullable = false, length = 50)
    val word: String
)

@Entity
@Table(name = "nouns")
class Noun(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(nullable = false, length = 50)
    val word: String
)
