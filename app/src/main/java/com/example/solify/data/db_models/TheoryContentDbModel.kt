package com.example.solify.data.db_models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "theory_contents",
    foreignKeys = [ForeignKey(
        entity = TheoryItemDbModel::class,
        parentColumns = ["id"],
        childColumns = ["theoryItemId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index(value = ["theoryItemId"])
    ]
)
data class TheoryContentDbModel(
    @PrimaryKey
    val id: String,
    val theoryItemId: String,
    val type: String,  // "text", "image", "audio"
    val text: String?,
    val imageUrl: String?,
    val audioUrl: String?,
    val caption: String?
)