package com.example.solify.data.local.mappers

import com.example.solify.data.local.db_models.LessonProgressDbModel
import com.example.solify.data.local.db_models.TestProgressDbModel
import com.example.solify.data.local.models.TestProgressWithLessonDbModel
import com.example.solify.data.local.db_models.UserProgressDbModel
import com.example.solify.data.remote.firebase.dto.TestProgressDto
import com.example.solify.domain.entities.progress.LessonProgress
import com.example.solify.domain.entities.progress.Status
import com.example.solify.domain.entities.progress.TestProgress
import com.example.solify.domain.entities.progress.UserProgress
import com.example.solify.domain.entities.progress.withDerivedStatus

fun String?.toProgressStatus(): Status {
    if (isNullOrBlank()) return Status.NOT_STARTED
    return runCatching { Status.valueOf(this) }.getOrDefault(Status.NOT_STARTED)
}

fun Status.toStorageString(): String = name

fun TestProgressWithLessonDbModel.toDomain(): TestProgress =
    TestProgress(
        testId = testId,
        completedQuestions = completedQuestions.toSet(),
        pendingQuestions = pendingQuestions,
        lessonId = lessonId,
        status = status.toProgressStatus()
    ).withDerivedStatus()

fun TestProgressDbModel.toDomain(lessonId: String? = null): TestProgress {
    return TestProgress(
        testId = testId,
        completedQuestions = completedQuestions.toSet(),
        pendingQuestions = pendingQuestions.toList(),
        lessonId = lessonId,
        status = status.toProgressStatus()
    ).withDerivedStatus()
}

fun TestProgress.toDbModel(userId: String, existingId: Long = 0): TestProgressDbModel {
    val normalized = withDerivedStatus()
    return TestProgressDbModel(
        id = existingId,
        userId = userId,
        testId = normalized.testId,
        completedQuestions = normalized.completedQuestions.toList(),
        pendingQuestions = normalized.pendingQuestions,
        status = normalized.status.toStorageString()
    )
}

fun TestProgressDto.toDomain(): TestProgress =
    TestProgress(
        testId = testId,
        completedQuestions = completedQuestions.toSet(),
        pendingQuestions = pendingQuestions,
        status = status.toProgressStatus()
    ).withDerivedStatus()

fun TestProgress.toDto(): TestProgressDto =
    TestProgressDto(
        testId = testId,
        completedQuestions = completedQuestions.toList(),
        pendingQuestions = pendingQuestions,
        status = status.toStorageString()
    )

// LessonProgress

fun LessonProgressDbModel.toDomain(
    testsProgress: List<TestProgressDbModel> = emptyList()
): LessonProgress {
    return LessonProgress(
        lessonId = lessonId,
        completedTests = completedTests.toSet(),
        pendingTests = pendingTests
    ).normalize()
}

fun LessonProgress.toDbModel(
    userId: String,
    lessonId: String
): LessonProgressDbModel {
    return LessonProgressDbModel(
        userId = userId,
        lessonId = lessonId,
        completedTests = completedTests.toList(),
        pendingTests = pendingTests.toList()
    )
}

// UserProgress

fun UserProgressDbModel.toDomain(
    lessonsProgress: List<LessonProgressDbModel> = emptyList()
): UserProgress {
    return UserProgress(
        userId = userId,
        completedLessons = completedLessons.toSet(),
    )
}

fun UserProgress.toDbModel(): UserProgressDbModel {
    return UserProgressDbModel(
        userId = userId,
        completedLessons = completedLessons.toList()
    )
}
