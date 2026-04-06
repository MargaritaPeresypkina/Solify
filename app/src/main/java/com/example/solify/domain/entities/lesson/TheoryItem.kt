package com.example.solify.domain.entities.lesson

data class TheoryItem(
    val id: String,
    val title: String,
    val description: String,
    val content: List<TheoryContent>
)


sealed class TheoryContent {
    data class Text(val id: String, val text: String): TheoryContent()
    data class Image(val id: String, val imageUrl: String, val caption: String? = null): TheoryContent()
    data class Audio(val id: String, val audioUrl: String, val caption: String? = null): TheoryContent()
}
