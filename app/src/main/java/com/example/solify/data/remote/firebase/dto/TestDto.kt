package com.example.solify.data.remote.firebase.dto

data class TestDto(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val questionsIds: List<String> = emptyList()
)
