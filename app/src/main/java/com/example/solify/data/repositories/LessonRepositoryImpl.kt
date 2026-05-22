package com.example.solify.data.repositories

import com.example.solify.data.local.data_sources.LessonLocalDataSource
import com.example.solify.data.local.db_models.LessonDbModel
import com.example.solify.data.remote.firebase.data_source.LessonRemoteDataSource
import com.example.solify.data.remote.firebase.data_source.QuestionRemoteDataSource
import com.example.solify.domain.entities.lesson.Lesson
import com.example.solify.domain.entities.lesson.Question
import com.example.solify.domain.entities.lesson.Test
import com.example.solify.domain.entities.lesson.TheoryItem
import com.example.solify.domain.repositories.LessonRepository
import com.example.solify.presentation.debug.AgentDebugLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LessonRepositoryImpl @Inject constructor(
    private val localDataSource: LessonLocalDataSource,
    private val remoteDataSource: LessonRemoteDataSource,
    private val questionRemoteDataSource: QuestionRemoteDataSource
) : LessonRepository {

    override fun observeAllLessons(): Flow<List<Lesson>> =
        localDataSource.getAllLessons().map { lessons ->
            lessons.sortedWith(compareBy({ it.level.ordinal }, { it.order }))
        }

    override fun observeTestIdsByLesson(): Flow<Map<String, List<String>>> =
        localDataSource.observeTestIdsByLesson()

    override suspend fun syncLessons(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val remoteLessons = remoteDataSource.getAllLessons().getOrElse { return@withContext Result.failure(it) }
            remoteLessons.forEach { lesson ->
                localDataSource.upsertLesson(
                    LessonDbModel(
                        id = lesson.id,
                        title = lesson.title,
                        description = lesson.description,
                        level = lesson.level.name,
                        order = lesson.order
                    )
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to sync lessons: ${e.message}", e))
        }
    }

    override suspend fun getLessonById(lessonId: String): Result<Lesson> {
        return try {
            val remoteLesson = remoteDataSource.getLessonById(lessonId).getOrNull()

            if (remoteLesson != null) {
                localDataSource.upsertLesson(
                    LessonDbModel(
                        id = remoteLesson.id,
                        title = remoteLesson.title,
                        description = remoteLesson.description,
                        level = remoteLesson.level.name,
                        order = remoteLesson.order
                    )
                )
                if (remoteLesson.theoryItems.isNotEmpty() || remoteLesson.tests.isNotEmpty()) {
                    localDataSource.upsertLessonContent(remoteLesson)
                }
                Result.success(remoteLesson)
            } else {
                val localLesson = localDataSource.getLessonById(lessonId)
                Result.success(localLesson)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load lesson: ${e.message}"))
        }
    }

    override suspend fun getTheoryItemById(theoryItemId: String): Result<TheoryItem> {
        return try {
            val localTheoryItem = localDataSource.getTheoryItemById(theoryItemId)
            if (localTheoryItem.content.isNotEmpty()) {
                return Result.success(localTheoryItem)
            }

            val lessonId = localDataSource.getTheoryItemLessonId(theoryItemId)
            if (lessonId != null) {
                val remoteTheoryItem = remoteDataSource
                    .getTheoryItemById(lessonId, theoryItemId)
                    .getOrNull()
                if (remoteTheoryItem != null && remoteTheoryItem.content.isNotEmpty()) {
                    localDataSource.upsertTheoryItem(remoteTheoryItem, lessonId)
                    return Result.success(remoteTheoryItem)
                }
            }

            Result.success(localTheoryItem)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load theory item: ${e.message}"))
        }
    }

    override suspend fun getTestById(testId: String, lessonId: String): Result<Test> {
        return try {
            var test = localDataSource.getTestById(testId)
            // #region agent log
            AgentDebugLog.log(
                hypothesisId = "A",
                location = "LessonRepositoryImpl.getTestById",
                message = "local test loaded",
                data = mapOf(
                    "testId" to testId,
                    "lessonId" to lessonId,
                    "localQuestionsIdsSize" to test.questionsIds.size
                )
            )
            // #endregion

            if (test.questionsIds.isEmpty()) {
                val remoteTest = questionRemoteDataSource.getTestById(lessonId, testId).getOrNull()
                // #region agent log
                AgentDebugLog.log(
                    hypothesisId = "A",
                    location = "LessonRepositoryImpl.getTestById",
                    message = "remote test fallback",
                    data = mapOf(
                        "testId" to testId,
                        "remoteQuestionsIdsSize" to (remoteTest?.questionsIds?.size ?: -1)
                    )
                )
                // #endregion
                if (remoteTest != null && remoteTest.questionsIds.isNotEmpty()) {
                    localDataSource.updateTestQuestionsIds(testId, lessonId, remoteTest.questionsIds)
                    test = test.copy(questionsIds = remoteTest.questionsIds)
                }
            }
            Result.success(test)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load test: ${e.message}"))
        }
    }

    override suspend fun getQuestionById(questionId: String, testId: String?): Result<Question> {
        return try {
            if (localDataSource.hasQuestion(questionId)) {
                val localQuestion = localDataSource.getQuestionById(questionId)
                // #region agent log
                AgentDebugLog.log(
                    hypothesisId = "C",
                    location = "LessonRepositoryImpl.getQuestionById",
                    message = "question from local cache",
                    data = mapOf("questionId" to questionId, "optionsCount" to localQuestion.options.size)
                )
                // #endregion
                return Result.success(localQuestion)
            }

            val remoteQuestion = questionRemoteDataSource.getQuestionById(questionId).getOrElse { error ->
                // #region agent log
                AgentDebugLog.log(
                    hypothesisId = "C",
                    location = "LessonRepositoryImpl.getQuestionById",
                    message = "remote question failed",
                    data = mapOf("questionId" to questionId, "error" to (error.message ?: "unknown"))
                )
                // #endregion
                return Result.failure(error)
            }

            val questionToCache = if (remoteQuestion.testId.isBlank() && !testId.isNullOrBlank()) {
                remoteQuestion.copy(testId = testId)
            } else {
                remoteQuestion
            }
            localDataSource.upsertQuestion(questionToCache)
            // #region agent log
            AgentDebugLog.log(
                hypothesisId = "C",
                location = "LessonRepositoryImpl.getQuestionById",
                message = "question fetched from firestore",
                data = mapOf(
                    "questionId" to questionId,
                    "testId" to remoteQuestion.testId,
                    "optionsCount" to remoteQuestion.options.size
                )
            )
            // #endregion
            Result.success(questionToCache)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load question: ${e.message}"))
        }
    }

    override suspend fun getHint(questionId: String): Result<String> {
        return try {
            if (!localDataSource.hasQuestion(questionId)) {
                getQuestionById(questionId)
            }
            val hint = localDataSource.getHint(questionId)
            Result.success(hint)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load hint: ${e.message}"))
        }
    }
}
