package com.example.solify.domain.usecases.lessons

import com.example.solify.data.local.dao.LessonDao
import com.example.solify.data.local.db_models.LessonDbModel
import com.example.solify.data.remote.firebase.data_source.LessonRemoteDataSource
import javax.inject.Inject

class SyncLessonsUseCase @Inject constructor(
    private val remoteDataSource: LessonRemoteDataSource,
    private val lessonDao: LessonDao
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            val remoteLessons = remoteDataSource.getAllLessons().getOrNull() ?: emptyList()

            remoteLessons.forEach { lesson ->
                val lessonDb = LessonDbModel(
                    id = lesson.id,
                    title = lesson.title,
                    description = lesson.description,
                    level = lesson.level.name,
                    order = 0
                )
                lessonDao.insertLesson(lessonDb)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}