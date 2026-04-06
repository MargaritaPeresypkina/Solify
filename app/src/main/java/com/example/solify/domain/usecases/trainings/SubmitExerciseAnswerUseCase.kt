package com.example.solify.domain.usecases.trainings

import com.example.solify.domain.entities.training.Exercise
import javax.inject.Inject

class SubmitExerciseAnswerUseCase @Inject constructor() {
    operator fun invoke(
        currentExercise: Exercise,
        selectedOptionId: String,
        currentIndex: Int,
        totalExercises: Int
    ): SubmitExerciseAnswerResult {
        val isCorrect = currentExercise.correctAnswerId == selectedOptionId
        val nextIndex = currentIndex + 1
        val isCompleted = nextIndex >= totalExercises

        return SubmitExerciseAnswerResult(
            isCorrect = isCorrect,
            correctOptionId = currentExercise.correctAnswerId,
            nextIndex = if (isCompleted) null else nextIndex,
            isCompleted = isCompleted
        )
    }
}

data class SubmitExerciseAnswerResult(
    val isCorrect: Boolean,
    val correctOptionId: String,
    val nextIndex: Int?,
    val isCompleted: Boolean
)