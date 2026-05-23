package com.example.solify.data.local.mappers

import com.example.solify.data.local.db_models.ExerciseAnswerOptionDbModel
import com.example.solify.data.local.db_models.ExerciseDbModel
import com.example.solify.data.local.db_models.TrainerDbModel
import com.example.solify.data.local.db_models.TrainingDbModel
import com.example.solify.data.local.models.ExerciseWithOptions
import com.example.solify.data.local.models.TrainingWithTrainers
import com.example.solify.domain.entities.training.Exercise
import com.example.solify.domain.entities.training.ExercisesAnswerOption
import com.example.solify.domain.entities.training.Trainer
import com.example.solify.domain.entities.training.Training
import com.example.solify.domain.entities.training.TrainingCategory

fun Exercise.toDbModel(trainerId: String): ExerciseDbModel {
    return ExerciseDbModel(
        id = id,
        trainerId = trainerId,
        text = text,
        audio = audio,
        correctOptionId = correctAnswerId
    )
}

fun ExerciseWithOptions.toDomain(): Exercise {
    return Exercise(
        id = exercise.id,
        text = exercise.text,
        audio = exercise.audio,
        options = options.map { it.toDomain() },
        correctAnswerId = exercise.correctOptionId
    )
}

fun ExercisesAnswerOption.toDbModel(exerciseId: String): ExerciseAnswerOptionDbModel {
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

fun Trainer.toDbModel(trainingId: String): TrainerDbModel {
    return TrainerDbModel(
        id = id,
        trainingId = trainingId,
        title = title,
        description = description,
        exercisesIds = exercisesIds
    )
}

fun TrainerDbModel.toDomain(): Trainer {
    return Trainer(
        id = id,
        description = description,
        title = title,
        exercisesIds = exercisesIds
    )
}

fun Training.toDbModel(): TrainingDbModel {
    return TrainingDbModel(
        id = id,
        title = title,
        description = description,
        imageUrl = imageUrl,
        category = category.name
    )
}

fun TrainingWithTrainers.toDomain(): Training {
    return Training(
        id = training.id,
        title = training.title,
        description = training.description,
        imageUrl = training.imageUrl,
        category = TrainingCategory.valueOf(training.category),
        trainers = trainers.map { it.toDomain() }
    )
}
