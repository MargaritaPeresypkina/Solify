package com.example.solify.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.solify.data.local.dao.LessonDao
import com.example.solify.data.local.dao.ProgressDao
import com.example.solify.data.local.dao.TrainingDao
import com.example.solify.data.local.dao.UserDao
import com.example.solify.data.local.db_models.AnswerOptionDbModel
import com.example.solify.data.local.db_models.ExerciseAnswerOptionDbModel
import com.example.solify.data.local.db_models.ExerciseDbModel
import com.example.solify.data.local.db_models.LessonDbModel
import com.example.solify.data.local.db_models.LessonProgressDbModel
import com.example.solify.data.local.db_models.QuestionDbModel
import com.example.solify.data.local.db_models.TestDbModel
import com.example.solify.data.local.db_models.TestProgressDbModel
import com.example.solify.data.local.db_models.TheoryContentDbModel
import com.example.solify.data.local.db_models.TheoryItemDbModel
import com.example.solify.data.local.db_models.TrainerDbModel
import com.example.solify.data.local.db_models.TrainingDbModel
import com.example.solify.data.local.db_models.UserDbModel
import com.example.solify.data.local.db_models.UserProgressDbModel

@Database(
    entities = [
        // Users
        UserDbModel::class,
        UserProgressDbModel::class,
        LessonProgressDbModel::class,
        TestProgressDbModel::class,
        // Lessons
        LessonDbModel::class,
        TheoryItemDbModel::class,
        TheoryContentDbModel::class,
        TestDbModel::class,
        QuestionDbModel::class,
        AnswerOptionDbModel::class,
        // Trainings
        TrainingDbModel::class,
        TrainerDbModel::class,
        ExerciseDbModel::class,
        ExerciseAnswerOptionDbModel::class
    ],
    version = 13,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun lessonDao(): LessonDao
    abstract fun trainingDao(): TrainingDao
    abstract fun progressDao(): ProgressDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        private val lock = Any()

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(lock) {
                instance ?: Room.databaseBuilder(
                    context = context.applicationContext,
                    klass = AppDatabase::class.java,
                    name = "solify.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
        }
    }
}