package com.example.solify.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
        ExerciseDbModel::class,
        ExerciseAnswerOptionDbModel::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun lessonDao(): LessonDao
    abstract fun trainingDao(): TrainingDao
    abstract fun progressDao(): ProgressDao

    companion object{

        private val instance: AppDatabase? = null
        private val lock = Any()

        fun getInstance(context: Context): AppDatabase {
             instance?.let {return it}

            synchronized(lock) {
                instance?.let { return it }

                return Room.databaseBuilder(
                    context = context,
                    klass = AppDatabase::class.java,
                    name = "solify.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
            }
        }
    }
}