package com.example.solify.domain.entities.progress

data class LessonProgress(
    val lessonId: String,
    val completedTests: Set<String> = emptySet(),
    val pendingTests: List<String> = emptyList()
) {
    /** Реальные невыполненные тесты (пустые строки из Firebase/Room игнорируются). */
    val activePendingTests: List<String>
        get() = pendingTests.filter { it.isNotBlank() }

    val isLessonCompleted: Boolean
        get() = completedTests.isNotEmpty() && activePendingTests.isEmpty()

    fun normalize(): LessonProgress =
        if (activePendingTests.size == pendingTests.size) {
            this
        } else {
            copy(pendingTests = activePendingTests)
        }

    fun toLessonStatus(): Status = when {
        completedTests.isEmpty() && activePendingTests.isEmpty() -> Status.NOT_STARTED
        isLessonCompleted -> Status.COMPLETED
        else -> Status.IN_PROGRESS
    }
}

fun LessonProgress?.toDisplayStatus(): Status = this?.toLessonStatus() ?: Status.NOT_STARTED
