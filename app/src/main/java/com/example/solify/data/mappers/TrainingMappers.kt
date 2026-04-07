package com.example.solify.data.mappers

import com.example.solify.data.db_models.ExerciseAnswerOptionDbModel
import com.example.solify.data.db_models.ExerciseDbModel
import com.example.solify.data.db_models.TrainingDbModel
import com.example.solify.data.models.ExerciseWithOptions
import com.example.solify.data.models.TrainingWithExercises
import com.example.solify.domain.entities.training.Exercise
import com.example.solify.domain.entities.training.ExercisesAnswerOption
import com.example.solify.domain.entities.training.Training
import com.example.solify.domain.entities.training.TrainingCategory

fun Exercise.toDbModel(trainingId: String): ExerciseDbModel{
    return ExerciseDbModel(
        id = id,
        trainingId = trainingId,
        text = text,
        audio = audio,
        correctAnswerId = correctAnswerId
    )
}
fun ExerciseWithOptions.toDomain(): Exercise {
    return Exercise(
        id = exercise.id,
        text = exercise.text,
        audio = exercise.audio,
        options = options.map { it.toDomain() },
        correctAnswerId = exercise.correctAnswerId
    )
}

fun ExercisesAnswerOption.toDbModel(exerciseId: String): ExerciseAnswerOptionDbModel{
    return ExerciseAnswerOptionDbModel(
        id = id,
        exerciseId = exerciseId,
        text = text,
        image = image
    )
}

fun ExerciseAnswerOptionDbModel.toDomain(): ExercisesAnswerOption {
    return ExercisesAnswerOption(
        id = id,
        text = text,
        image = image
    )
}

fun Training.toDbModel(): TrainingDbModel{
    return TrainingDbModel(
        id = id,
        title = title,
        description = description,
        imageUrl = imageUrl,
        category = category.name
    )
}


fun TrainingWithExercises.toDomain(): Training {
    return Training(
        id = training.id,
        title = training.title,
        description = training.description,
        imageUrl = training.imageUrl,
        category = TrainingCategory.valueOf(training.category),
        exercises = exercises.map { it.toDomain() }
    )
}
