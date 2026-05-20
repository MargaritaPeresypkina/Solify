package com.example.solify.data.remote.firebase.mappers

import com.example.solify.data.local.db_models.TestDbModel
import com.example.solify.data.local.db_models.TheoryItemDbModel
import com.example.solify.data.remote.firebase.dto.TestDto
import com.example.solify.data.remote.firebase.dto.TheoryItemDto
import com.example.solify.domain.entities.lesson.Test
import com.example.solify.domain.entities.lesson.TheoryItem

fun TheoryItemDto.toDomain(): TheoryItem = TheoryItem(
    id = id,
    title = title,
    description = description,
    content = emptyList(),
    order = order
)

fun TheoryItemDto.toDbModel(lessonId: String): TheoryItemDbModel = TheoryItemDbModel(
    id = id,
    lessonId = lessonId,
    title = title,
    description = description,
    order = order
)

fun TestDto.toDomain(): Test = Test(
    id = id,
    title = title,
    description = description,
    questionsIds = questionsIds
)

fun TestDto.toDbModel(lessonId: String): TestDbModel = TestDbModel(
    id = id,
    lessonId = lessonId,
    title = title,
    description = description
)

fun TheoryItem.toDbModel(lessonId: String): TheoryItemDbModel = TheoryItemDbModel(
    id = id,
    lessonId = lessonId,
    title = title,
    description = description,
    order = order
)

fun Test.toDbModel(lessonId: String): TestDbModel = TestDbModel(
    id = id,
    lessonId = lessonId,
    title = title,
    description = description
)
