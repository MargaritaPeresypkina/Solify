package com.example.solify.data.db_models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lessons")
data class LessonDbModel(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val level: String, // BEGINNER, INTERMEDIATE, ADVANCED
)