package com.example.solify.domain.repositories

import com.example.solify.domain.entities.lesson.Lesson
import com.example.solify.domain.entities.lesson.Question
import com.example.solify.domain.entities.lesson.Test
import com.example.solify.domain.entities.lesson.TheoryItem
import kotlinx.coroutines.flow.Flow

interface LessonRepository {
    suspend fun getLessonById(lessonId: String): Result<Lesson>

    fun getAllLessons(): Flow<List<Lesson>>

    suspend fun getTheoryItemById(
        theoryItemId: String
    ): Result<TheoryItem>

    suspend fun getTestById(testId: String): Result<Test>

    suspend fun getQuestionById(questionId: String): Result<Question>

    suspend fun getHint(questionId: String): Result<String>
}