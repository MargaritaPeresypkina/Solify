package com.example.solify.domain.sync

import com.example.solify.domain.usecases.lessons.SyncLessonsUseCase
import com.example.solify.domain.usecases.progress.SyncAllProgressUseCase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDataSyncCoordinator @Inject constructor(
    private val syncLessonsUseCase: SyncLessonsUseCase,
    private val syncAllProgressUseCase: SyncAllProgressUseCase
) {
    private val mutex = Mutex()
    @Volatile
    private var syncedForUserId: String? = null

    suspend fun ensureSynced(userId: String) {
        if (syncedForUserId == userId) return
        mutex.withLock {
            if (syncedForUserId == userId) return@withLock
            runCatching { syncLessonsUseCase() }
            runCatching { syncAllProgressUseCase(userId) }
            syncedForUserId = userId
        }
    }

    fun reset() {
        syncedForUserId = null
    }
}
