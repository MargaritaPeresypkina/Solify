package com.example.solify.data.mappers

import com.example.solify.data.db_models.AnswerOptionDbModel
import com.example.solify.data.db_models.ContentType
import com.example.solify.data.db_models.LessonDbModel
import com.example.solify.data.db_models.TestDbModel
import com.example.solify.data.db_models.TheoryContentDbModel
import com.example.solify.data.db_models.TheoryItemDbModel
import com.example.solify.data.models.LessonWithContentDbModel
import com.example.solify.data.models.QuestionWithOptionsDbModel
import com.example.solify.data.models.TheoryItemWithContentDbModel
import com.example.solify.domain.entities.lesson.AnswerOption
import com.example.solify.domain.entities.lesson.Lesson
import com.example.solify.domain.entities.lesson.Level
import com.example.solify.domain.entities.lesson.Question
import com.example.solify.domain.entities.lesson.Test
import com.example.solify.domain.entities.lesson.TheoryContent
import com.example.solify.domain.entities.lesson.TheoryItem

// AnswerOption

fun List<AnswerOptionDbModel>.toAnswerOptionsDomain(): List<AnswerOption> {
    return map { answerOptionDbModel ->
        AnswerOption(
            id = answerOptionDbModel.id,
            text = answerOptionDbModel.text
        )
    }
}

// TheoryContent

fun List<TheoryContentDbModel>.toTheoryContentsDomain(): List<TheoryContent> {
    return map { theoryContentDbModel ->
        when(theoryContentDbModel.type) {
            ContentType.IMAGE ->
                TheoryContent.Image(
                    imageUrl = theoryContentDbModel.content
                )
            ContentType.TEXT ->
                TheoryContent.Text(
                    text = theoryContentDbModel.content
                )
            ContentType.AUDIO ->
                TheoryContent.Audio(
                    audioUrl = theoryContentDbModel.content
                )
        }
    }
}

// TheoryItem

fun List<TheoryItemDbModel>.toTheoryItemsDomain(): List<TheoryItem> {
    return map { theoryItemDbModel ->
        TheoryItem(
            id = theoryItemDbModel.id,
            title = theoryItemDbModel.title,
            description = theoryItemDbModel.description,
            content = emptyList(),
            order = theoryItemDbModel.order
        )
    }
}

fun TheoryItemWithContentDbModel.toDomain(): TheoryItem {
    return TheoryItem(
            id = theoryItem.id,
            title = theoryItem.title,
            description = theoryItem.description,
            content = contents.toTheoryContentsDomain(),
            order = theoryItem.order
        )
}

// Question
fun QuestionWithOptionsDbModel.toDomain(): Question {
    return Question(
            id = question.id,
            text = question.text,
            imageUrl = question.imageUrl,
            options = options.toAnswerOptionsDomain(),
            correctOptionId = question.correctOptionId,
            hint = question.hint
        )
}


// Test


fun List<TestDbModel>.toTestsDomain(): List<Test> {
    return map { testsWithQuestions ->
        Test(
            id = testsWithQuestions.id,
            title = testsWithQuestions.title,
            description = testsWithQuestions.description,
            questionsIds = emptyList()
        )
    }
}

fun TestDbModel.toDomain(questionsIds: List<String>): Test {
    return Test(
            id = id,
            title = title,
            description = description,
            questionsIds = questionsIds
        )
}

// Lesson

fun LessonWithContentDbModel.toDomain(): Lesson {
    return Lesson(
            id = lesson.id,
            title = lesson.title,
            description = lesson.description,
            level = Level.valueOf(lesson.level),
            theoryItems = theoryItems.toTheoryItemsDomain(),
            tests = tests.toTestsDomain()
        )
}

fun List<LessonDbModel>.toLessonsDomain(): List<Lesson> {
    return map { lessonDbModel ->
        Lesson(
            id = lessonDbModel.id,
            title = lessonDbModel.title,
            description = lessonDbModel.description,
            level = Level.valueOf(lessonDbModel.level),
            theoryItems = emptyList(),
            tests = emptyList()
        )
    }
}




