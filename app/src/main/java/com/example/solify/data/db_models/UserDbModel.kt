package com.example.solify.data.db_models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserDbModel(
    @PrimaryKey
    val id: String,
    val name: String,
    val surname: String,
    val email: String,
    val avatarUrl: String?,
    val passwordHash: String
)