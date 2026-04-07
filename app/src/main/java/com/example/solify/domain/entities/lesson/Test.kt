package com.example.solify.domain.entities.lesson

data class Test(
    val id: String,
    val title: String,
    val description: String,
    val questionsIds: List<String>
)