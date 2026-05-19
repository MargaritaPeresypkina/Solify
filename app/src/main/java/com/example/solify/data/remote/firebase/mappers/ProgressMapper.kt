package com.example.solify.data.remote.firebase.mappers

import com.example.solify.data.remote.firebase.dto.LessonProgressDto
import com.example.solify.domain.entities.progress.LessonProgress

fun LessonProgressDto.toDomain(): LessonProgress =
    LessonProgress(
        lessonId = lessonId,
        completedTests = completedTests.toSet(),
        pendingTests = pendingTests
    ).normalize()