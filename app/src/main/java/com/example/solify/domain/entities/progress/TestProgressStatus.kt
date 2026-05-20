package com.example.solify.domain.entities.progress

fun resolveTestStatus(
    testId: String,
    testProgress: TestProgress?,
    lessonProgress: LessonProgress?
): Status {
    if (lessonProgress?.completedTests?.contains(testId) == true) {
        return Status.COMPLETED
    }
    val progress = testProgress
    if (progress == null ||
        (progress.completedQuestions.isEmpty() && progress.pendingQuestions.isEmpty())
    ) {
        return Status.NOT_STARTED
    }
    return Status.IN_PROGRESS
}
