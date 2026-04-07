package com.example.solify.domain.entities.lesson

data class TheoryItem(
    val id: String,
    val title: String,
    val description: String,
    val content: List<TheoryContent>,
    val order: Int
)


sealed class TheoryContent {
    data class Text(val text: String): TheoryContent()
    data class Image(val imageUrl: String): TheoryContent()
    data class Audio(val audioUrl: String): TheoryContent()
}
