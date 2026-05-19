package com.example.solify.domain.usecases.lessons

import com.example.solify.domain.repositories.LessonRepository
import javax.inject.Inject

class SyncLessonsUseCase @Inject constructor(
    private val lessonRepository: LessonRepository
) {
    suspend operator fun invoke(): Result<Unit> = lessonRepository.syncLessons()
}
