package com.example.solify.presentation.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

/**
 * Converts plain text from Firebase into [AnnotatedString] with inline styles.
 *
 * Supported markers (edit markers here if you need different syntax):
 * - `*fragment*` — bold
 * - `_fragment_` — underline
 * - `<fragment>` — accent color ([accentColor])
 */
object TheoryTextFormatter {

    fun parse(
        text: String,
        accentColor: Color
    ): AnnotatedString {
        val builder = AnnotatedString.Builder()
        var index = 0

        while (index < text.length) {
            when {
                text.startsWith("*", index) -> {
                    val end = text.indexOf('*', startIndex = index + 1)
                    if (end > index + 1) {
                        val fragment = text.substring(index + 1, end)
                        builder.pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                        builder.append(fragment)
                        builder.pop()
                        index = end + 1
                        continue
                    }
                }

                text.startsWith("_", index) -> {
                    val end = text.indexOf('_', startIndex = index + 1)
                    if (end > index + 1) {
                        val fragment = text.substring(index + 1, end)
                        builder.pushStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                        builder.append(fragment)
                        builder.pop()
                        index = end + 1
                        continue
                    }
                }

                text.startsWith("<", index) -> {
                    val end = text.indexOf('>', startIndex = index + 1)
                    if (end > index + 1) {
                        val fragment = text.substring(index + 1, end)
                        builder.pushStyle(SpanStyle(color = accentColor, fontWeight = FontWeight.SemiBold))
                        builder.append(fragment)
                        builder.pop()
                        index = end + 1
                        continue
                    }
                }
            }

            builder.append(text[index])
            index++
        }

        return builder.toAnnotatedString()
    }
}
