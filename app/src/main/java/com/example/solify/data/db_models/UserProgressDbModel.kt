package com.example.solify.data.db_models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "user_progress",
    foreignKeys = [ForeignKey(
        entity = UserDbModel::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    primaryKeys = ["userId"],
    indices = [Index(value = ["userId"], unique = true)]
)
data class UserProgressDbModel(
    val userId: String,
    val completedLessons: String  //"lessonId1,lessonId2,lessonId3"
)