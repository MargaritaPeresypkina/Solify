package com.example.solify.data.models

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
data class TheoryItemWithContents(
    val theoryItem: TheoryItemDbModel,
    val contents: List<TheoryContentDbModel>
)

data class QuestionWithOptions(
    val question: QuestionDbModel,
    val options: List<AnswerOptionDbModel>
)

data class TestWithQuestions(
    val test: TestDbModel,
    val questions: List<QuestionWithOptions>
)

data class LessonWithAllData(
    val lesson: LessonDbModel,
    val theoryItems: List<TheoryItemWithContents>,
    val tests: List<TestWithQuestions>
)


data class ExerciseWithOptions(
    val exercise: ExerciseDbModel,
    val options: List<ExerciseAnswerOptionDbModel>
)

data class TrainingWithAllData(
    val training: TrainingDbModel,
    val exercises: List<ExerciseWithOptions>
)
