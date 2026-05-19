package com.example.solify.data.remote.firebase.mappers

import com.example.solify.data.remote.firebase.dto.LessonDto
import com.example.solify.domain.entities.lesson.Lesson
import com.example.solify.domain.entities.lesson.Level

fun LessonDto.toDomain(): Lesson {
    return Lesson(
        id = id,
        title = title,
        description = description,
        level = Level.valueOf(level),
        theoryItems = emptyList(),
        tests = emptyList()
    )
}