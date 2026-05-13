package com.example.solify.data.remote.firebase.data_source

import android.util.Log
import com.example.solify.data.remote.firebase.dto.LessonProgressDto
import com.example.solify.data.remote.firebase.dto.TestProgressDto
import com.google.firebase.firestore.FirebaseFirestore
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
                        completedQuestions = (data["completedQuestions"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        pendingQuestions = (data["pendingQuestions"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
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
                            completedQuestions = (data["completedQuestions"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                            pendingQuestions = (data["pendingQuestions"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
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

    suspend fun updateTestProgress(userId: String, progress: TestProgressDto): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val data = hashMapOf(
                    "completedQuestions" to progress.completedQuestions,
                    "pendingQuestions" to progress.pendingQuestions
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
}