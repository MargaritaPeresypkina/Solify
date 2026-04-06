package com.example.solify.domain.repositories

import com.example.solify.domain.entities.lesson.Question
import com.example.solify.domain.entities.lesson.Test

interface TestRepository {

    suspend fun getTestById(testId: String): Result<Test>

    suspend fun getQuestionById(questionId: String): Result<Question>

    suspend fun getHint(questionId: String, testId: String): Result<String>
}