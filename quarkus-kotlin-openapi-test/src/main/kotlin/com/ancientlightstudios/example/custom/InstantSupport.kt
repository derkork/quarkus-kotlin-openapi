package com.ancientlightstudios.example.custom

import com.ancientlightstudios.quarkus.kotlin.openapi.*
import com.fasterxml.jackson.databind.JsonNode
import java.time.Instant


fun Maybe<String?>.asInstant(): Maybe<Instant?> = onNotNull {
    try {
        success(Instant.parse(value))
    } catch (e: Exception) {
        failure(ValidationError("Invalid date: $value", context))
    }
}

@JvmName("asInstantFromJson")
fun Maybe<JsonNode?>.asInstant(): Maybe<Instant?> = asString().asInstant()

fun Instant.toJson(): JsonNode = toString().asJson()