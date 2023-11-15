package com.ancientlightstudios.example.custom

import com.ancientlightstudios.quarkus.kotlin.openapi.*
import com.fasterxml.jackson.databind.JsonNode
import java.util.*

fun Maybe<String?>.asStringUuid(): Maybe<UUID?> = onNotNull {
    try {
        UUID.fromString(value).asMaybe(context)
    } catch (e: IllegalArgumentException) {
        Maybe.Failure(context, ValidationError("Invalid UUID: $value", context))
    }
}

@JvmName("asStringUuidFromNode")
fun Maybe<JsonNode?>.asStringUuid(): Maybe<UUID?> = asString().asStringUuid()


fun UUID.fromStringUuid(): JsonNode = toString().fromString()