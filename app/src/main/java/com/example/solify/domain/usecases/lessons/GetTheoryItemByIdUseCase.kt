package com.example.solify.domain.usecases.lessons

import com.example.solify.domain.entities.lesson.TheoryItem
import com.example.solify.domain.repositories.LessonRepository
import jakarta.inject.Inject

class GetTheoryItemByIdUseCase @Inject constructor(
    private val repository: LessonRepository
) {
    suspend operator fun invoke(theoryItemId: String): Result<TheoryItem>{
        return repository.getTheoryItemById(theoryItemId = theoryItemId)
    }
}