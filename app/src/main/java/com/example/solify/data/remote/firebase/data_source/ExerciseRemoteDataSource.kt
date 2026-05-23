package com.example.solify.data.remote.firebase.data_source

import com.example.solify.data.remote.firebase.dto.ExerciseAnswerOptionDto
import com.example.solify.data.remote.firebase.dto.ExerciseDto
import com.example.solify.domain.entities.training.Exercise
import com.example.solify.domain.entities.training.ExercisesAnswerOption
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun getExerciseById(exerciseId: String, trainerId: String): Result<Exercise> {
        return withContext(Dispatchers.IO) {
            try {
                val exerciseRef = firestore.collection("exercises").document(exerciseId)
                val document = exerciseRef.get().await()

                if (!document.exists()) {
                    return@withContext Result.failure(
                        Exception("Exercise not found in Firestore: $exerciseId")
                    )
                }

                val exerciseDto = document.toObject(ExerciseDto::class.java)
                    ?: return@withContext Result.failure(
                        Exception("Invalid exercise document: $exerciseId")
                    )

                val optionsSnapshot = exerciseRef.collection("answer_options").get().await()
                val options = optionsSnapshot.documents.mapNotNull { optionDocument ->
                    val optionDto = optionDocument.toObject(ExerciseAnswerOptionDto::class.java)
                        ?: return@mapNotNull null
                    ExercisesAnswerOption(
                        id = optionDocument.id,
                        text = optionDto.text,
                        image = optionDto.image
                    )
                }

                Result.success(
                    Exercise(
                        id = document.id,
                        text = exerciseDto.text,
                        audio = exerciseDto.audio,
                        options = options,
                        correctAnswerId = exerciseDto.correctOptionId
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
