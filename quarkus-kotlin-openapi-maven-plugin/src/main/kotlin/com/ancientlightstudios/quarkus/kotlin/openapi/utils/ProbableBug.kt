package com.ancientlightstudios.quarkus.kotlin.openapi.utils

@Suppress("FunctionName")
fun ProbableBug(reason: String): Nothing {
    val location = IllegalArgumentException().stackTrace
        .drop(1).firstOrNull()
        ?.run { "$fileName:$lineNumber" }
        ?: "unknown location"
    throw IllegalStateException("Bug detected ($location): $reason")
}