package com.example.solify.data.db_models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_progress",
    foreignKeys = [ForeignKey(
        entity = UserDbModel::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["userId"], unique = true)]
)
data class UserProgressDbModel(
    @PrimaryKey
    val userId: String,
    val completedLessons: String  //"lessonId1,lessonId2,lessonId3"
)