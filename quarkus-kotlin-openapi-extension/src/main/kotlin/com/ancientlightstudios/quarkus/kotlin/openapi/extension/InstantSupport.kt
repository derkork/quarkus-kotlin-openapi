package com.ancientlightstudios.quarkus.kotlin.openapi.extension

import com.ancientlightstudios.quarkus.kotlin.openapi.*
import com.fasterxml.jackson.databind.JsonNode
import java.time.Instant

fun Maybe<String?>.asInstant(): Maybe<Instant?> = onNotNull {
    try {
        success(Instant.parse(value))
    } catch (e: Exception) {
        failure(ValidationError("Invalid instant: $value", context))
    }
}

@JvmName("asInstantFromJson")
fun Maybe<JsonNode?>.asInstant(): Maybe<Instant?> = asString().asInstant()

@JvmName("asStringFromInstant")
fun Instant.asString(): String = toString()

fun Instant.asJson(): JsonNode = asString().asJson()