package com.example.solify.data.remote.firebase.data_source

import android.util.Log
import com.example.solify.data.remote.firebase.dto.DailyActivityDto
import com.example.solify.data.remote.firebase.dto.ExerciseProgressDto
import com.example.solify.data.remote.firebase.dto.LessonProgressDto
import com.example.solify.data.remote.firebase.dto.TestProgressDto
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getLessonProgress(userId: String, lessonId: String): Result<LessonProgressDto> {
        return withContext(Dispatchers.IO) {
            try {
                val document = firestore
                    .collection("users")
                    .document(userId)
                    .collection("lesson_progress")
                    .document(lessonId)
                    .get()
                    .await()

                val data = document.data
                if (data != null) {
                    val progress = LessonProgressDto(
                        lessonId = document.id,
                        completedTests = (data["completedTests"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        pendingTests = (data["pendingTests"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    )
                    Result.success(progress)
                } else {
                    Result.success(LessonProgressDto(lessonId = lessonId))
                }
            } catch (e: Exception) {
                Log.e("ProgressRemote", "Error loading lesson progress", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getAllLessonsProgress(userId: String): Result<List<LessonProgressDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = firestore
                    .collection("users")
                    .document(userId)
                    .collection("lesson_progress")
                    .get()
                    .await()

                val lessons = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data
                    if (data != null) {
                        LessonProgressDto(
                            lessonId = doc.id,
                            completedTests = (data["completedTests"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                            pendingTests = (data["pendingTests"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                        )
                    } else null
                }
                Log.d("LessonsDebug", "Remote lessons progress: $lessons")
                Result.success(lessons)
            } catch (e: Exception) {
                Log.e("ProgressRemote", "Error loading all lessons progress", e)
                Result.failure(e)
            }
        }
    }

    suspend fun updateLessonProgress(userId: String, progress: LessonProgressDto): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val data = hashMapOf(
                    "completedTests" to progress.completedTests,
                    "pendingTests" to progress.pendingTests
                )
                firestore
                    .collection("users")
                    .document(userId)
                    .collection("lesson_progress")
                    .document(progress.lessonId)
                    .set(data)
                    .await()
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("ProgressRemote", "Error updating lesson progress", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getTestProgress(userId: String, testId: String): Result<TestProgressDto> {
        return withContext(Dispatchers.IO) {
            try {
                val document = firestore
                    .collection("users")
                    .document(userId)
                    .collection("test_progress")
                    .document(testId)
                    .get()
                    .await()

                val data = document.data
                if (data != null) {
                    val progress = TestProgressDto(
                        testId = document.id,
                        completedQuestions = readStringList(data, "completedQuestions", "completedTests"),
                        pendingQuestions = readStringList(data, "pendingQuestions", "pendingTests"),
                        status = readStatus(data)
                    )
                    Result.success(progress)
                } else {
                    Result.success(TestProgressDto(testId = testId))
                }
            } catch (e: Exception) {
                Log.e("ProgressRemote", "Error loading test progress", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getAllTestsProgress(userId: String): Result<List<TestProgressDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = firestore
                    .collection("users")
                    .document(userId)
                    .collection("test_progress")
                    .get()
                    .await()

                val tests = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data
                    if (data != null) {
                        TestProgressDto(
                            testId = doc.id,
                            completedQuestions = readStringList(data, "completedQuestions", "completedTests"),
                            pendingQuestions = readStringList(data, "pendingQuestions", "pendingTests"),
                            status = readStatus(data)
                        )
                    } else null
                }
                Result.success(tests)
            } catch (e: Exception) {
                Log.e("ProgressRemote", "Error loading all tests progress", e)
                Result.failure(e)
            }
        }
    }

    private fun readStringList(data: Map<String, Any>, vararg keys: String): List<String> {
        keys.forEach { key ->
            val list = (data[key] as? List<*>)?.filterIsInstance<String>()
            if (!list.isNullOrEmpty()) return list
        }
        return emptyList()
    }

    private fun readStatus(data: Map<String, Any>): String {
        val raw = data["status"] as? String
        if (!raw.isNullOrBlank()) return raw
        val completed = readStringList(data, "completedQuestions", "completedTests")
        return if (completed.isNotEmpty()) "IN_PROGRESS" else "NOT_STARTED"
    }

    suspend fun updateTestProgress(userId: String, progress: TestProgressDto): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val data = hashMapOf(
                    "completedQuestions" to progress.completedQuestions,
                    "pendingQuestions" to progress.pendingQuestions,
                    "status" to progress.status
                )
                firestore
                    .collection("users")
                    .document(userId)
                    .collection("test_progress")
                    .document(progress.testId)
                    .set(data)
                    .await()
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("ProgressRemote", "Error updating test progress", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getExerciseProgress(userId: String, trainerId: String): Result<ExerciseProgressDto> {
        return withContext(Dispatchers.IO) {
            try {
                val document = firestore
                    .collection("users")
                    .document(userId)
                    .collection("exercise_progress")
                    .document(trainerId)
                    .get()
                    .await()

                val data = document.data
                if (data != null) {
                    Result.success(
                        ExerciseProgressDto(
                            trainerId = document.id,
                            completedExercises = readStringList(data, "completedExercises"),
                            pendingExercises = readStringList(data, "pendingExercises"),
                            status = readExerciseStatus(data)
                        )
                    )
                } else {
                    Result.success(ExerciseProgressDto(trainerId = trainerId))
                }
            } catch (e: Exception) {
                Log.e("ProgressRemote", "Error loading exercise progress", e)
                Result.failure(e)
            }
        }
    }

    suspend fun updateExerciseProgress(userId: String, progress: ExerciseProgressDto): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val data = hashMapOf(
                    "completedExercises" to progress.completedExercises,
                    "pendingExercises" to progress.pendingExercises,
                    "status" to progress.status
                )
                firestore
                    .collection("users")
                    .document(userId)
                    .collection("exercise_progress")
                    .document(progress.trainerId)
                    .set(data)
                    .await()
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("ProgressRemote", "Error updating exercise progress", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getDailyActivitySince(
        userId: String,
        sinceDate: String
    ): Result<List<DailyActivityDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = firestore
                    .collection("users")
                    .document(userId)
                    .collection("daily_activity")
                    .whereGreaterThanOrEqualTo(
                        com.google.firebase.firestore.FieldPath.documentId(),
                        sinceDate
                    )
                    .get()
                    .await()

                val activities = snapshot.documents.mapNotNull { doc ->
                    val count = (doc.getLong("completedTestsCount") ?: 0L).toInt()
                    DailyActivityDto(date = doc.id, completedTestsCount = count)
                }
                Result.success(activities)
            } catch (e: Exception) {
                Log.e("ProgressRemote", "Error loading daily activity", e)
                Result.failure(e)
            }
        }
    }

    suspend fun incrementDailyActivity(userId: String, date: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                firestore
                    .collection("users")
                    .document(userId)
                    .collection("daily_activity")
                    .document(date)
                    .set(
                        mapOf("completedTestsCount" to FieldValue.increment(1)),
                        SetOptions.merge()
                    )
                    .await()
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("ProgressRemote", "Error incrementing daily activity", e)
                Result.failure(e)
            }
        }
    }

    suspend fun upsertDailyActivity(userId: String, activity: DailyActivityDto): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                firestore
                    .collection("users")
                    .document(userId)
                    .collection("daily_activity")
                    .document(activity.date)
                    .set(
                        mapOf("completedTestsCount" to activity.completedTestsCount),
                        SetOptions.merge()
                    )
                    .await()
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("ProgressRemote", "Error upserting daily activity", e)
                Result.failure(e)
            }
        }
    }

    private fun readExerciseStatus(data: Map<String, Any>): String {
        val raw = data["status"] as? String
        if (!raw.isNullOrBlank()) return raw
        val completed = readStringList(data, "completedExercises")
        val pending = readStringList(data, "pendingExercises")
        return when {
            completed.isNotEmpty() && pending.isEmpty() -> "COMPLETED"
            completed.isNotEmpty() || pending.isNotEmpty() -> "IN_PROGRESS"
            else -> "NOT_STARTED"
        }
    }
}