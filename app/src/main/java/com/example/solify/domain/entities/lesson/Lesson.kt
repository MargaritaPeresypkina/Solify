package com.example.solify.domain.entities.lesson

data class Lesson(
    val id: String,
    val title: String,
    val description: String,
    val level: Level,
    val theoryItems: List<TheoryItem>,
    val tests: List<Test>
)

enum class Level { BEGINNER, INTERMEDIATE, ADVANCED }