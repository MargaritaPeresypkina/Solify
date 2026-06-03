package com.example.solify.data.local.db_models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "daily_activity",
    foreignKeys = [
        ForeignKey(
            entity = UserDbModel::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["userId", "date"],
    indices = [Index(value = ["userId", "date"], unique = true)]
)
data class DailyActivityDbModel(
    val userId: String,
    /** Calendar day in yyyy-MM-dd (device timezone). */
    val date: String,
    val completedTestsCount: Int
)
