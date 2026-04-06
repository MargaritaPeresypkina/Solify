package com.example.solify.data.mappers

import com.example.solify.data.db_models.LessonProgressDbModel
import com.example.solify.data.db_models.TestProgressDbModel
import com.example.solify.data.db_models.UserProgressDbModel
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
        lessonId = lessonId,
        testId = testId,
        completedQuestions = completedQuestions.toCommaString(),
        pendingQuestions = pendingQuestions.toCommaString()
    )
}

// LessonProgress

fun LessonProgressDbModel.toDomain(
    testsProgress: List<TestProgressDbModel> = emptyList()
): LessonProgress {
    return LessonProgress(
        lessonId = lessonId,
        completedTests = completedTests.toSet(),
//        testProgress = testsProgress.associate {
//            it.testId to it.toDomain()
//        }
    )
}

fun LessonProgress.toDbModel(
    userId: String,
    lessonId: String
): LessonProgressDbModel {
    return LessonProgressDbModel(
        userId = userId,
        lessonId = lessonId,
        completedTests = completedTests.toCommaString()
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
        completedLessons = completedLessons.toCommaString()
    )
}