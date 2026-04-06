package com.example.solify.data.mappers

fun String.toSet(): Set<String> {
    return if (isEmpty()) emptySet() else split(",").toSet()
}

fun String.toList(): List<String> {
    return if (isEmpty()) emptyList() else split(",")
}

fun Set<String>.toCommaString(): String {
    return joinToString(",")
}

fun List<String>.toCommaString(): String {
    return joinToString(",")
}