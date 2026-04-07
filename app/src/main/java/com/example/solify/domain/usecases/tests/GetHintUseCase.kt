package com.example.solify.domain.usecases.tests

import com.example.solify.domain.repositories.LessonRepository
import jakarta.inject.Inject

class GetHintUseCase @Inject constructor(
    private val repository: LessonRepository
) {
    suspend operator fun invoke(
        questionId: String
    ): Result<String> {
        return repository.getHint(questionId)
    }
}