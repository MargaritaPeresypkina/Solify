package com.example.solify.data.repositories

import com.example.solify.data.dao.LessonDao
import com.example.solify.data.mappers.toDomain
import com.example.solify.domain.entities.lesson.Lesson
import com.example.solify.domain.entities.lesson.Test
import com.example.solify.domain.entities.lesson.TheoryItem
import com.example.solify.domain.repositories.LessonRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LessonRepositoryImpl @Inject constructor(
    private val lessonDao: LessonDao
) : LessonRepository {

    override suspend fun getLessonById(lessonId: String): Result<Lesson> {
        return try {
            val lessonDb = lessonDao.getLessonById(lessonId)
                ?: return Result.failure(IllegalArgumentException("Lesson not found"))

            val theoryItemsDb = lessonDao.getTheoryItemsByLesson(lessonId)
            val theoryItems = theoryItemsDb.map { theoryItemDb ->
                TheoryItem(
                    id = theoryItemDb.id,
                    title = theoryItemDb.title,
                    description = theoryItemDb.description,
                    content = emptyList()
                )
            }

            val testsDb = lessonDao.getTestsByLesson(lessonId)
            val tests = testsDb.map { testDb ->
                Test(
                    id = testDb.id,
                    title = testDb.title,
                    description = testDb.description,
                    questions = emptyList()
                )
            }

            Result.success(
                Lesson(
                    id = lessonDb.id,
                    title = lessonDb.title,
                    description = lessonDb.description,
                    level = enumValueOf(lessonDb.level),
                    theoryItems = theoryItems,
                    tests = tests
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load lesson: ${e.message}"))
        }
    }

    override fun getAllLessons(): Flow<List<Lesson>> {
        return lessonDao.getAllLessons().map { it.map { lessonDb ->
                Lesson(
                    id = lessonDb.id,
                    title = lessonDb.title,
                    description = lessonDb.description,
                    level = enumValueOf(lessonDb.level),
                    theoryItems = emptyList(),
                    tests = emptyList()
                )
            }
        }
    }

    override suspend fun getTheoryItemById(theoryItemId: String): Result<TheoryItem> {
        return try {
            val theoryItemDb = lessonDao.getTheoryItemById(theoryItemId)
                ?: return Result.failure(IllegalArgumentException("Theory item not found"))

            val contents = lessonDao.getTheoryContentsByItem(theoryItemId)

            Result.success(
                TheoryItem(
                    id = theoryItemDb.id,
                    title = theoryItemDb.title,
                    description = theoryItemDb.description,
                    content = contents.map { it.toDomain() }
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load theory item: ${e.message}"))
        }
    }

    override suspend fun getTheoryItemsByLesson(lessonId: String): Result<List<TheoryItem>> {
        return try {
            val theoryItemsDb = lessonDao.getTheoryItemsByLesson(lessonId)
            val theoryItems = theoryItemsDb.map { theoryItemDb ->
                TheoryItem(
                    id = theoryItemDb.id,
                    title = theoryItemDb.title,
                    description = theoryItemDb.description,
                    content = emptyList()
                )
            }
            Result.success(theoryItems)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load theory items: ${e.message}"))
        }
    }
}