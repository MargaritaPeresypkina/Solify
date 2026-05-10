package com.example.solify.data.remote.firebase.mappers

import com.example.solify.data.remote.firebase.dto.UserDto
import com.example.solify.domain.entities.user.User
import kotlin.text.ifEmpty

fun User.toDto() : UserDto {
    return UserDto(
        id = id,
        name = name,
        surname = surname,
        email = email,
        avatarUrl = avatarUrl ?: "",
        passwordHash = passwordHash
    )
}

fun UserDto.toDomain(): User {
    return User(
        id = id,
        name = name,
        surname = surname,
        email = email,
        avatarUrl = avatarUrl.ifEmpty { null },
        passwordHash = passwordHash
    )
}