package com.example.solify.data.models

import androidx.room.Embedded
import androidx.room.Relation
import com.example.solify.data.db_models.AnswerOptionDbModel
import com.example.solify.data.db_models.ExerciseAnswerOptionDbModel
import com.example.solify.data.db_models.ExerciseDbModel
import com.example.solify.data.db_models.LessonDbModel
import com.example.solify.data.db_models.QuestionDbModel
import com.example.solify.data.db_models.TestDbModel
import com.example.solify.data.db_models.TheoryContentDbModel
import com.example.solify.data.db_models.TheoryItemDbModel
import com.example.solify.data.db_models.TrainingDbModel


// для связанных данных
data class TheoryItemWithContentDbModel(
    @Embedded
    val theoryItem: TheoryItemDbModel,
    @Relation(
        parentColumn = "id",
        entityColumn = "theoryItemId"
    )
    val contents: List<TheoryContentDbModel>
)

data class QuestionWithOptionsDbModel(
    @Embedded
    val question: QuestionDbModel,
    @Relation(
        parentColumn = "id",
        entityColumn = "questionId"
    )
    val options: List<AnswerOptionDbModel>
)

data class TestWithQuestionsDbModel(
    @Embedded
    val test: TestDbModel,
    @Relation(
        parentColumn = "id",
        entityColumn = "testId"
    )
    val questions: List<QuestionDbModel>
)

data class LessonWithContentDbModel(
    @Embedded
    val lesson: LessonDbModel,
    @Relation(
        parentColumn = "id",
        entityColumn = "lessonId"
    )
    val theoryItems: List<TheoryItemDbModel>,
    @Relation(
        parentColumn = "id",
        entityColumn = "lessonId"
    )
    val tests: List<TestDbModel>
)

data class ExerciseWithOptions(
    val exercise: ExerciseDbModel,
    val options: List<ExerciseAnswerOptionDbModel>
)

data class TrainingWithExercises(
    val training: TrainingDbModel,
    val exercises: List<ExerciseWithOptions>
)
