package com.example.solify.data.local.data_sources

import com.example.solify.data.local.dao.LessonDao
import com.example.solify.data.local.db_models.LessonDbModel
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

            lessonDb.toDomain()
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to get lesson $lessonId", e)
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


    suspend fun getTestById(testId: String): Test {
        return try {
            val testDb = lessonDao.getTestById(testId)
                ?: throw DomainException.NotFound("Test not found: $testId")

            val questionsIds = lessonDao.getQuestionsIdsByTest(testId)
            testDb.toDomain(questionsIds)
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to get test $testId", e)
        }
    }


    // Questions
    suspend fun getQuestionById(questionId: String): Question {
        return try {
            val questionDb = lessonDao.getQuestionById(questionId)
            questionDb.toDomain()
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to get question $questionId", e)
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

    suspend fun insertLesson(lesson: LessonDbModel) {
        try {
            lessonDao.insertLesson(lesson)
        } catch (e: Exception) {
            throw DataSourceException.DatabaseError("Failed to insert lesson", e)
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