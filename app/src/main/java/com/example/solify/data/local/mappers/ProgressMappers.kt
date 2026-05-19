package com.example.solify.data.local.mappers

import com.example.solify.data.local.db_models.LessonProgressDbModel
import com.example.solify.data.local.db_models.TestProgressDbModel
import com.example.solify.data.local.db_models.UserProgressDbModel
import com.example.solify.domain.entities.progress.LessonProgress
import com.example.solify.domain.entities.progress.TestProgress
import com.example.solify.domain.entities.progress.UserProgress

// TestProgress

fun TestProgressDbModel.toDomain(): TestProgress {
    return TestProgress(
        testId = testId,
        completedQuestions = completedQuestions.toSet(),
        pendingQuestions = pendingQuestions.toList()
    )
}

fun TestProgress.toDbModel(
    userId: String,
    lessonId: String,
    testId: String
): TestProgressDbModel {
    return TestProgressDbModel(
        userId = userId,
        testId = testId,
        completedQuestions = completedQuestions.toList(),
        pendingQuestions = pendingQuestions
    )
}

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
//        lessonProgress = lessonsProgress.associate {
//            it.lessonId to it.toDomain()
//        }
    )
}

fun UserProgress.toDbModel(): UserProgressDbModel {
    return UserProgressDbModel(
        userId = userId,
        completedLessons = completedLessons.toList()
    )
}