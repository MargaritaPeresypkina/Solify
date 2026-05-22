package com.example.solify.data.local.data_sources

import com.example.solify.data.local.dao.LessonDao
import com.example.solify.data.local.db_models.AnswerOptionDbModel
import com.example.solify.data.local.db_models.LessonDbModel
import com.example.solify.data.local.db_models.QuestionDbModel
import com.example.solify.data.remote.firebase.mappers.toDbModel
import com.example.solify.data.local.mappers.toDomain
import com.example.solify.data.local.mappers.toLessonsDomain
import com.example.solify.domain.entities.lesson.Lesson
import com.example.solify.domain.entities.lesson.Question
import com.example.solify.domain.entities.lesson.Test
import com.example.solify.domain.entities.lesson.TheoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LessonLocalDataSource @Inject constructor(
    private val lessonDao: LessonDao
) {

    // Lessons
    fun observeTestIdsByLesson(): Flow<Map<String, List<String>>> =
        lessonDao.observeAllTests().map { tests ->
            tests.groupBy({ it.lessonId }, { it.id })
        }

    fun getAllLessons(): Flow<List<Lesson>> =
        lessonDao.getAllLessons().map { lessons ->
            try {
                lessons.toLessonsDomain()
            } catch (e: Exception) {
                throw DataSourceException.MappingError("Failed to map lessons", e)
            }
    }

    suspend fun getLessonById(lessonId: String): Lesson {
        return try {
            val lessonDb = lessonDao.getLessonById(lessonId)
                ?: throw DomainException.NotFound("Lesson not found: $lessonId")

            val lesson = lessonDb.toDomain()
            val testsWithQuestionIds = lesson.tests.map { test ->
                val storedIds = lessonDao.getTestById(test.id)?.questionsIds.orEmpty()
                val questionsIds = storedIds.ifEmpty { lessonDao.getQuestionsIdsByTest(test.id) }
                if (questionsIds.isEmpty()) test else test.copy(questionsIds = questionsIds)
            }
            lesson.copy(tests = testsWithQuestionIds)
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to get lesson $lessonId", e)
        }
    }

    suspend fun upsertLessonContent(lesson: Lesson) {
        try {
            lessonDao.deleteTheoryItemsForLesson(lesson.id)
            lessonDao.deleteTestsForLesson(lesson.id)
            if (lesson.theoryItems.isNotEmpty()) {
                lessonDao.insertTheoryItems(
                    lesson.theoryItems.map { it.toDbModel(lesson.id) }
                )
                val theoryContents = lesson.theoryItems.flatMap { item ->
                    item.content.mapIndexed { index, content ->
                        content.toDbModel(theoryItemId = item.id, order = index)
                    }
                }
                if (theoryContents.isNotEmpty()) {
                    lessonDao.insertTheoryContents(theoryContents)
                }
            }
            if (lesson.tests.isNotEmpty()) {
                lessonDao.insertTests(
                    lesson.tests.map { it.toDbModel(lesson.id) }
                )
            }
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to upsert lesson content", e)
        }
    }


    suspend fun getTheoryItemById(theoryItemId: String): TheoryItem {
        return try {
            val theoryItemDb = lessonDao.getTheoryItemById(theoryItemId)
                ?: throw DomainException.NotFound("Theory item not found: $theoryItemId")

            theoryItemDb.toDomain()
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to get theory item $theoryItemId", e)
        }
    }

    suspend fun getTheoryItemLessonId(theoryItemId: String): String? {
        return lessonDao.getTheoryItemById(theoryItemId)?.theoryItem?.lessonId
    }

    suspend fun upsertTheoryItem(theoryItem: TheoryItem, lessonId: String) {
        try {
            lessonDao.deleteTheoryContentsForItem(theoryItem.id)
            lessonDao.insertTheoryItems(listOf(theoryItem.toDbModel(lessonId)))
            val theoryContents = theoryItem.content.mapIndexed { index, content ->
                content.toDbModel(theoryItemId = theoryItem.id, order = index)
            }
            if (theoryContents.isNotEmpty()) {
                lessonDao.insertTheoryContents(theoryContents)
            }
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to upsert theory item", e)
        }
    }


    suspend fun getTestById(testId: String): Test {
        return try {
            val testDb = lessonDao.getTestById(testId)
                ?: throw DomainException.NotFound("Test not found: $testId")

            val questionsIds = testDb.questionsIds.ifEmpty {
                lessonDao.getQuestionsIdsByTest(testId)
            }
            testDb.toDomain(questionsIds)
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to get test $testId", e)
        }
    }


    // Questions
    suspend fun hasQuestion(questionId: String): Boolean =
        lessonDao.questionExists(questionId) > 0

    suspend fun getQuestionById(questionId: String): Question {
        return try {
            val questionDb = lessonDao.getQuestionById(questionId)
                ?: throw DomainException.NotFound("Question not found: $questionId")
            questionDb.toDomain()
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to get question $questionId", e)
        }
    }

    suspend fun upsertQuestion(question: Question) {
        try {
            lessonDao.insertQuestion(
                QuestionDbModel(
                    id = question.id,
                    testId = question.testId,
                    text = question.text,
                    imageUrl = question.imageUrl,
                    hint = question.hint,
                    correctOptionId = question.correctOptionId
                )
            )
            if (question.options.isNotEmpty()) {
                lessonDao.insertAnswerOptions(
                    question.options.map { option ->
                        AnswerOptionDbModel(
                            id = option.id,
                            questionId = question.id,
                            text = option.text
                        )
                    }
                )
            }
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to upsert question ${question.id}", e)
        }
    }

    suspend fun updateTestQuestionsIds(testId: String, lessonId: String, questionsIds: List<String>) {
        try {
            val testDb = lessonDao.getTestById(testId) ?: return
            lessonDao.insertTests(
                listOf(testDb.copy(questionsIds = questionsIds))
            )
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to update test questions ids", e)
        }
    }

    suspend fun getHint(questionId: String): String {
        return try {
            lessonDao.getHint(questionId)
                ?: throw DomainException.NotFound("Hint not found for question: $questionId")
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to get hint for $questionId", e)
        }
    }

    suspend fun upsertLesson(lesson: LessonDbModel) {
        try {
            val rowId = lessonDao.insertLesson(lesson)
            if (rowId == -1L) {
                lessonDao.updateLesson(lesson)
            }
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to upsert lesson", e)
        }
    }

}

sealed class DataSourceException(message: String, cause: Throwable?) : Exception(message, cause) {
    class DatabaseError(message: String, cause: Throwable?) : DataSourceException(message, cause)
    class MappingError(message: String, cause: Throwable?) : DataSourceException(message, cause)
}

sealed class DomainException(message: String) : Exception(message) {
    class NotFound(message: String) : DomainException(message)
}