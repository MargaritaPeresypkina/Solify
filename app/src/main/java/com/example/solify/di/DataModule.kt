package com.example.solify.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.solify.data.dao.LessonDao
import com.example.solify.data.dao.ProgressDao
import com.example.solify.data.dao.TrainingDao
import com.example.solify.data.dao.UserDao
import com.example.solify.data.database.AppDatabase
import com.example.solify.data.repositories.LessonRepositoryImpl
import com.example.solify.data.repositories.ProgressRepositoryImpl
import com.example.solify.data.repositories.TrainingRepositoryImpl
import com.example.solify.data.repositories.UserRepositoryImpl
import com.example.solify.data.session.SessionManagerImpl
import com.example.solify.domain.repositories.LessonRepository
import com.example.solify.domain.repositories.ProgressRepository
import com.example.solify.domain.repositories.TrainingRepository
import com.example.solify.domain.repositories.UserRepository
import com.example.solify.domain.session.SessionManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Singleton
    @Binds
    fun bindLessonRepository(
        impl: LessonRepositoryImpl
    ): LessonRepository

    @Singleton
    @Binds
    fun bindProgressRepository(
        impl: ProgressRepositoryImpl
    ): ProgressRepository

    @Singleton
    @Binds
    fun bindTrainingRepository(
        impl: TrainingRepositoryImpl
    ): TrainingRepository

    @Singleton
    @Binds
    fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository

    @Singleton
    @Binds
    fun bindSessionManager(
        impl: SessionManagerImpl
    ): SessionManager

    companion object {

        @Provides
        @Singleton
        fun provideContext(@ApplicationContext context: Context): Context {
            return context
        }

        @Singleton
        @Provides
        fun provideDatabase(
           @ApplicationContext context: Context
        ): AppDatabase {
            return AppDatabase.getInstance(context)
        }

        @Singleton
        @Provides
        fun providesUserDao(
            database: AppDatabase
        ): UserDao{
            return database.userDao()
        }

        @Singleton
        @Provides
        fun providesLessonDao(
            database: AppDatabase
        ): LessonDao {
            return database.lessonDao()
        }

        @Singleton
        @Provides
        fun providesProgressDao(
            database: AppDatabase
        ): ProgressDao {
            return database.progressDao()
        }

        @Singleton
        @Provides
        fun providesTrainingDao(
            database: AppDatabase
        ): TrainingDao {
            return database.trainingDao()
        }

        @Provides
        @Singleton
        fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
            return PreferenceDataStoreFactory.create(
                corruptionHandler = null,
                migrations = listOf(),
                scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
                produceFile = { context.preferencesDataStoreFile("user_preferences") }
            )
        }

    }
}