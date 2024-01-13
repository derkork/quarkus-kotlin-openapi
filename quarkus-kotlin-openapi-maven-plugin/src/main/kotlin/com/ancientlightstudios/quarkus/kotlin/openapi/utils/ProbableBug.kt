package com.ancientlightstudios.quarkus.kotlin.openapi.utils

@Suppress("NOTHING_TO_INLINE", "FunctionName")
inline fun ProbableBug(): Nothing = throw IllegalStateException("Bug detected")

@Suppress("NOTHING_TO_INLINE", "FunctionName")
inline fun ProbableBug(reason: String): Nothing = throw IllegalStateException("Bug detected: $reason")