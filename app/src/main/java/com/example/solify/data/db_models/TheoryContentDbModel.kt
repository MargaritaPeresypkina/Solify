package com.example.solify.data.db_models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "theory_contents",
    foreignKeys = [ForeignKey(
        entity = TheoryItemDbModel::class,
        parentColumns = ["id"],
        childColumns = ["theoryItemId"],
        onDelete = ForeignKey.CASCADE
    )],
    primaryKeys = ["theoryItemId", "order"],
    indices = [
        Index(value = ["theoryItemId"])
    ]
)
data class TheoryContentDbModel(
    val theoryItemId: String,
    val type: ContentType,
    val content: String,
    val order: Int
)

enum class ContentType {
    IMAGE, TEXT, AUDIO
}