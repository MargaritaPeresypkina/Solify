package com.example.solify.data.remote.firebase.data_source

import com.example.solify.data.remote.firebase.dto.AnswerOptionDto
import com.example.solify.data.remote.firebase.dto.QuestionDto
import com.example.solify.domain.entities.lesson.AnswerOption
import com.example.solify.domain.entities.lesson.Question
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun getTestById(lessonId: String, testId: String): Result<com.example.solify.domain.entities.lesson.Test> {
        return withContext(Dispatchers.IO) {
            try {
                val document = firestore
                    .collection("lessons")
                    .document(lessonId)
                    .collection("tests")
                    .document(testId)
                    .get()
                    .await()

                if (!document.exists()) {
                    return@withContext Result.failure(Exception("Test not found in Firestore: $testId"))
                }

                val questionsIds = (document.get("questionsIds") as? List<*>)
                    ?.filterIsInstance<String>()
                    .orEmpty()

                val title = document.getString("title").orEmpty()
                val description = document.getString("description").orEmpty()

                Result.success(
                    com.example.solify.domain.entities.lesson.Test(
                        id = document.id,
                        title = title,
                        description = description,
                        questionsIds = questionsIds
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getQuestionById(questionId: String): Result<Question> {
        return withContext(Dispatchers.IO) {
            try {
                val questionRef = firestore.collection("questions").document(questionId)
                val document = questionRef.get().await()

                if (!document.exists()) {
                    return@withContext Result.failure(Exception("Question not found: $questionId"))
                }

                val questionDto = document.toObject(QuestionDto::class.java)
                    ?: return@withContext Result.failure(Exception("Invalid question document: $questionId"))

                val optionsSnapshot = questionRef.collection("answer_options").get().await()
                val options = optionsSnapshot.documents.mapNotNull { optionDocument ->
                    val optionDto = optionDocument.toObject(AnswerOptionDto::class.java)
                        ?: return@mapNotNull null
                    AnswerOption(
                        id = optionDocument.id,
                        text = optionDto.text
                    )
                }

                Result.success(
                    Question(
                        id = document.id,
                        text = questionDto.text,
                        imageUrl = questionDto.imageUrl,
                        options = options,
                        correctOptionId = questionDto.correctOptionId,
                        hint = questionDto.hint,
                        testId = questionDto.testId
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
