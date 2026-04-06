package com.example.solify.domain.entities.user

import androidx.compose.runtime.Immutable

@Immutable
data class User(
    val id: String,
    val name: String,
    val surname: String,
    val email: String,
    val avatarUrl: String? = null,
    val passwordHash: String
)