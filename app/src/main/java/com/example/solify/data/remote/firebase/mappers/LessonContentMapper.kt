package com.example.solify.data.remote.firebase.mappers

import com.example.solify.data.local.db_models.ContentType
import com.example.solify.data.local.db_models.TestDbModel
import com.example.solify.data.local.db_models.TheoryContentDbModel
import com.example.solify.data.local.db_models.TheoryItemDbModel
import com.example.solify.data.remote.firebase.dto.TestDto
import com.example.solify.data.remote.firebase.dto.TheoryContentDto
import com.example.solify.data.remote.firebase.dto.TheoryItemDto
import com.example.solify.domain.entities.lesson.Test
import com.example.solify.domain.entities.lesson.TheoryContent
import com.example.solify.domain.entities.lesson.TheoryItem

fun TheoryItemDto.toDomain(contents: List<TheoryContentDto> = emptyList()): TheoryItem = TheoryItem(
    id = id,
    title = title,
    description = description,
    content = contents.toTheoryContentsDomain(),
    order = order
)

fun List<TheoryContentDto>.toTheoryContentsDomain(): List<TheoryContent> =
    sortedBy { it.order }.mapNotNull { it.toDomain() }

fun TheoryContentDto.toDomain(): TheoryContent? = when (type.trim().lowercase()) {
    "text" -> TheoryContent.Text(content)
    "image" -> TheoryContent.Image(imageUrl = content)
    "audio" -> TheoryContent.Audio(audioUrl = content)
    else -> null
}

fun TheoryContent.toDbModel(theoryItemId: String, order: Int): TheoryContentDbModel = when (this) {
    is TheoryContent.Text -> TheoryContentDbModel(
        theoryItemId = theoryItemId,
        type = ContentType.TEXT,
        content = text,
        order = order
    )
    is TheoryContent.Image -> TheoryContentDbModel(
        theoryItemId = theoryItemId,
        type = ContentType.IMAGE,
        content = imageUrl,
        order = order
    )
    is TheoryContent.Audio -> TheoryContentDbModel(
        theoryItemId = theoryItemId,
        type = ContentType.AUDIO,
        content = audioUrl,
        order = order
    )
}

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
    description = description,
    questionsIds = questionsIds
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
    description = description,
    questionsIds = questionsIds
)
