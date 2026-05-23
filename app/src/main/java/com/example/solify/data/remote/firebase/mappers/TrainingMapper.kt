package com.example.solify.data.remote.firebase.mappers

import com.example.solify.data.local.db_models.TrainingDbModel
import com.example.solify.data.local.db_models.TrainerDbModel
import com.example.solify.data.remote.firebase.dto.TrainerDto
import com.example.solify.data.remote.firebase.dto.TrainingDto
import com.example.solify.domain.entities.training.Trainer
import com.example.solify.domain.entities.training.Training
import com.example.solify.domain.entities.training.TrainingCategory

fun TrainingDto.toDomain(trainers: List<Trainer> = emptyList()): Training {
    return Training(
        id = id,
        title = title,
        description = description,
        imageUrl = imageUrl.takeIf { it.isNotBlank() },
        category = runCatching { TrainingCategory.valueOf(category.uppercase()) }
            .getOrDefault(TrainingCategory.EAR),
        trainers = trainers
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

fun TrainerDto.toDomain(): Trainer {
    return Trainer(
        id = id,
        description = description,
        title = title,
        exercisesIds = exercisesIds,
        order = order
    )
}

fun Trainer.toDbModel(trainingId: String): TrainerDbModel {
    return TrainerDbModel(
        id = id,
        trainingId = trainingId,
        title = title,
        description = description,
        exercisesIds = exercisesIds,
        order = order
    )
}

fun TrainingDbModel.toListItemDomain(): Training {
    return Training(
        id = id,
        title = title,
        description = description,
        imageUrl = imageUrl,
        category = TrainingCategory.valueOf(category),
        trainers = emptyList()
    )
}
