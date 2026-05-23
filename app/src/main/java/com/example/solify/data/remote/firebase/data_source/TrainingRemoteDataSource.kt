package com.example.solify.data.remote.firebase.data_source

import android.util.Log
import com.example.solify.data.remote.firebase.dto.TrainerDto
import com.example.solify.data.remote.firebase.dto.TrainingDto
import com.example.solify.data.remote.firebase.mappers.toDomain
import com.example.solify.domain.entities.training.Training
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrainingRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun getAllTrainings(): Result<List<Training>> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = firestore.collection("trainings").get().await()
                val trainings = snapshot.documents.mapNotNull { document ->
                    val trainingDto = document.toObject(TrainingDto::class.java)
                        ?.copy(id = document.id)
                        ?: return@mapNotNull null

                    val trainers = loadTrainers(document.reference.path)
                    trainingDto.toDomain(trainers)
                }
                Log.d("TrainingRemote", "Loaded ${trainings.size} trainings")
                Result.success(trainings)
            } catch (e: Exception) {
                Log.e("TrainingRemote", "Error loading trainings", e)
                Result.failure(e)
            }
        }
    }

    private suspend fun loadTrainers(trainingPath: String): List<com.example.solify.domain.entities.training.Trainer> {
        val snapshot = firestore.collection("$trainingPath/trainers").get().await()
        return snapshot.documents.mapNotNull { document ->
            val exercisesIds = (document.get("exercisesIds") as? List<*>)
                ?.filterIsInstance<String>()
                .orEmpty()
            val dto = document.toObject(TrainerDto::class.java) ?: return@mapNotNull null
            val order = document.getLong("order")?.toInt()
                ?: (document.get("order") as? Number)?.toInt()
                ?: dto.order
            dto.copy(
                id = document.id,
                exercisesIds = exercisesIds,
                order = order
            ).toDomain()
        }.sortedBy { it.order }
    }
}
