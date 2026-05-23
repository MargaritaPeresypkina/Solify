package com.example.solify.domain.entities.progress

data class ExerciseProgress(
    val trainerId: String,
    val completedExercises: Set<String> = emptySet(),
    val pendingExercises: List<String> = emptyList(),
    val status: Status = Status.NOT_STARTED
)

fun ExerciseProgress.withDerivedStatus(): ExerciseProgress {
    val derived = when {
        completedExercises.isNotEmpty() && pendingExercises.isEmpty() -> Status.COMPLETED
        completedExercises.isNotEmpty() || pendingExercises.isNotEmpty() -> Status.IN_PROGRESS
        else -> Status.NOT_STARTED
    }
    return if (status == derived) this else copy(status = derived)
}
