package com.example.solify.domain.entities.progress

data class LessonProgress(
    val lessonId: String,
    val completedTests: Set<String> = emptySet(),
    val pendingTests: List<String> = emptyList()
) {
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

    /** @deprecated Use [resolveLessonStatus] with test progress for UI. */
    fun toLessonStatus(): Status = when {
        isLessonCompleted -> Status.COMPLETED
        else -> Status.NOT_STARTED
    }
}
