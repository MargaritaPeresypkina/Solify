package com.example.solify.domain.utils

import kotlinx.coroutines.flow.Flow

suspend fun <T> Flow<T>.value(): T? {
    var result: T? = null
    collect { result = it }
    return result
}