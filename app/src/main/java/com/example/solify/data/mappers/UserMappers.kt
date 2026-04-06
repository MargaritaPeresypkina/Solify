package com.example.solify.data.mappers

import com.example.solify.data.db_models.UserDbModel
import com.example.solify.domain.entities.user.User

fun User.toDbModel(): UserDbModel {
    return UserDbModel(
        id, name, surname, email, avatarUrl, passwordHash
    )
}

fun UserDbModel.toDomain(): User {
    return User(
        id, name, surname, email, avatarUrl, passwordHash
    )
}

