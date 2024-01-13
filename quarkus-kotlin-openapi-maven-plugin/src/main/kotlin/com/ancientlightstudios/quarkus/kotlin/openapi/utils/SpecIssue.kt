package com.ancientlightstudios.quarkus.kotlin.openapi.utils

@Suppress("NOTHING_TO_INLINE", "FunctionName")
inline fun SpecIssue(message: String): Nothing = throw IllegalStateException("Spec issue: $message")