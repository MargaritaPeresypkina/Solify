package com.example.solify.data.mappers

import com.example.solify.data.db_models.AnswerOptionDbModel
import com.example.solify.data.db_models.LessonDbModel
import com.example.solify.data.db_models.QuestionDbModel
import com.example.solify.data.db_models.TestDbModel
import com.example.solify.data.db_models.TheoryContentDbModel
import com.example.solify.data.db_models.TheoryItemDbModel
import com.example.solify.data.models.LessonWithAllData
import com.example.solify.data.models.QuestionWithOptions
import com.example.solify.data.models.TestWithQuestions
import com.example.solify.data.models.TheoryItemWithContents
import com.example.solify.domain.entities.lesson.AnswerOption
import com.example.solify.domain.entities.lesson.Lesson
import com.example.solify.domain.entities.lesson.Level
import com.example.solify.domain.entities.lesson.Question
import com.example.solify.domain.entities.lesson.Test
import com.example.solify.domain.entities.lesson.TheoryContent
import com.example.solify.domain.entities.lesson.TheoryItem

// AnswerOption

fun AnswerOption.toDbModel(questionId: String): AnswerOptionDbModel {
    return AnswerOptionDbModel(
        id = id,
        questionId = questionId,
        text = text
    )
}

fun AnswerOptionDbModel.toDomain(): AnswerOption {
    return AnswerOption(
        id = id,
        text = text
    )
}

// TheoryContent

fun TheoryContent.toDbModel(theoryItemId: String): TheoryContentDbModel {
    return when (this) {
        is TheoryContent.Text -> TheoryContentDbModel(
            id = id,
            theoryItemId = theoryItemId,
            type = "text",
            text = text,
            imageUrl = null,
            audioUrl = null,
            caption = null
        )
        is TheoryContent.Image -> TheoryContentDbModel(
            id = id,
            theoryItemId = theoryItemId,
            type = "image",
            text = null,
            imageUrl = imageUrl,
            audioUrl = null,
            caption = caption
        )
        is TheoryContent.Audio -> TheoryContentDbModel(
            id = id,
            theoryItemId = theoryItemId,
            type = "audio",
            text = null,
            imageUrl = null,
            audioUrl = audioUrl,
            caption = caption
        )
    }
}

fun TheoryContentDbModel.toDomain(): TheoryContent {
    return when (type) {
        "text" -> TheoryContent.Text(
            id = id,
            text = text ?: ""
        )
        "image" -> TheoryContent.Image(
            id = id,
            imageUrl = imageUrl ?: "",
            caption = caption
        )
        "audio" -> TheoryContent.Audio(
            id = id,
            audioUrl = audioUrl ?: "",
            caption = caption
        )
        else -> throw IllegalArgumentException("Unknown content type: $type")
    }
}

// TheoryItem

fun TheoryItem.toDbModel(lessonId: String): TheoryItemDbModel {
    return TheoryItemDbModel(
        id = id,
        lessonId = lessonId,
        title = title,
        description = description
    )
}


fun TheoryItemWithContents.toDomain(): TheoryItem {
    return TheoryItem(
        id = theoryItem.id,
        title = theoryItem.title,
        description = theoryItem.description,
        content = contents.map { it.toDomain() }
    )
}

// Question

fun Question.toDbModel(testId: String): QuestionDbModel {
    return QuestionDbModel(
        id = id,
        testId = testId,
        text = text,
        imageUrl = imageUrl,
        hint = hint,
        correctOptionId = correctOptionId
    )
}
fun QuestionWithOptions.toDomain(): Question {
    return Question(
        id = question.id,
        text = question.text,
        imageUrl = question.imageUrl,
        options = options.map { it.toDomain() },
        correctOptionId = question.correctOptionId,
        hint = question.hint
    )
}


// Test

fun Test.toDbModel(lessonId: String): TestDbModel {
    return TestDbModel(
        id = id,
        lessonId = lessonId,
        title = title,
        description = description
    )
}

fun TestWithQuestions.toDomain(): Test {
    return Test(
        id = test.id,
        title = test.title,
        description = test.description,
        questions = questions.map { it.toDomain() }
    )
}

// Lesson

fun Lesson.toDbModel(): LessonDbModel {
    return LessonDbModel(
        id = id,
        title = title,
        description = description,
        level = level.name
    )
}

fun LessonWithAllData.toDomain(): Lesson {
    return Lesson(
        id = lesson.id,
        title = lesson.title,
        description = lesson.description,
        level = Level.valueOf(lesson.level),
        theoryItems = theoryItems.map { it.toDomain() },
        tests = tests.map { it.toDomain() }
    )
}




