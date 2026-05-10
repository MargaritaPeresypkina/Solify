package com.example.solify.data.remote.firebase.dto

import com.google.firebase.firestore.DocumentId

data class UserDto(
    @get:DocumentId
    val id: String = "",
    val name: String = "",
    val surname: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val passwordHash: String = ""
)