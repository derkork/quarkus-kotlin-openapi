package com.ancientlightstudios.quarkus.kotlin.openapi.utils

@Suppress("NOTHING_TO_INLINE", "FunctionName")
inline fun ConfigIssue(message: String): Nothing = throw IllegalStateException("Config issue: $message")