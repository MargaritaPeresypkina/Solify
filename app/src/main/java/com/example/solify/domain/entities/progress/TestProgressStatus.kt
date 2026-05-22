package com.example.solify.domain.entities.progress

/**
 * Derives persisted status from question lists (same rules as [resolveTestStatus] for UI).
 */
fun TestProgress.withDerivedStatus(): TestProgress {
    val derived = when {
        completedQuestions.isNotEmpty() -> Status.IN_PROGRESS
        else -> Status.NOT_STARTED
    }
    return if (status == derived) this else copy(status = derived)
}

fun resolveTestStatus(
    testId: String,
    testProgress: TestProgress?,
    lessonProgress: LessonProgress?
): Status {
    if (lessonProgress?.completedTests?.contains(testId) == true) {
        return Status.COMPLETED
    }
    if (testProgress?.completedQuestions?.isNotEmpty() == true) {
        return Status.IN_PROGRESS
    }
    return Status.NOT_STARTED
}

fun resolveLessonStatus(
    lessonTestIds: List<String>,
    lessonProgress: LessonProgress?,
    testsProgress: List<TestProgress>
): Status {
    if (lessonTestIds.isNotEmpty() && lessonProgress != null &&
        lessonTestIds.all { lessonProgress.completedTests.contains(it) }
    ) {
        return Status.COMPLETED
    }
    if (lessonProgress != null && lessonProgress.isLessonCompleted && lessonTestIds.isEmpty()) {
        return Status.COMPLETED
    }
    if (hasAnyCompletedQuestion(lessonTestIds, testsProgress)) {
        return Status.IN_PROGRESS
    }
    return Status.NOT_STARTED
}

private fun hasAnyCompletedQuestion(
    lessonTestIds: List<String>,
    testsProgress: List<TestProgress>
): Boolean {
    if (lessonTestIds.isEmpty()) return false
    return testsProgress.any { progress ->
        progress.testId in lessonTestIds && progress.completedQuestions.isNotEmpty()
    }
}
