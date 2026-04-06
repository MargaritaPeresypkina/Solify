package com.example.solify.domain.usecases.tests

import com.example.solify.domain.repositories.TestRepository
import jakarta.inject.Inject

class GetHintUseCase @Inject constructor(
    private val repository: TestRepository
) {
    suspend operator fun invoke(
        testId: String,
        questionId: String
    ): Result<String> {
        return repository.getHint(questionId, testId)
    }
}