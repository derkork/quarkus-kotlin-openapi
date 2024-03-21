package com.ancientlightstudios.example.custom

import com.ancientlightstudios.quarkus.kotlin.openapi.*
import com.fasterxml.jackson.databind.JsonNode
import java.util.*

fun Maybe<String?>.asUuid(): Maybe<UUID?> = onNotNull {
    try {
        success(UUID.fromString(value))
    } catch (e: IllegalArgumentException) {
        failure(ValidationError("Invalid UUID: $value", context))
    }
}

@JvmName("asUuidFromJson")
fun Maybe<JsonNode?>.asUuid(): Maybe<UUID?> = asString().asUuid()

fun UUID.asJson(): JsonNode = toString().asJson()