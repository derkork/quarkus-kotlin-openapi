package com.ancientlightstudios.quarkus.kotlin.openapi.utils

@Suppress("NOTHING_TO_INLINE", "FunctionName")
public inline fun ProbableBug(): Nothing = throw NotImplementedError("Bug detected")

@Suppress("NOTHING_TO_INLINE", "FunctionName")
public inline fun ProbableBug(reason: String): Nothing = throw NotImplementedError("Bug detected: $reason")