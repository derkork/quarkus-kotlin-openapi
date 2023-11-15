package com.ancientlightstudios.example.custom

import com.ancientlightstudios.quarkus.kotlin.openapi.*
import com.fasterxml.jackson.databind.JsonNode
import java.time.Instant


fun Maybe<String?>.asStringDate(): Maybe<Instant?> = onNotNull {
    try {
        Instant.parse(value).asMaybe(context)
    } catch (e: Exception) {
        Maybe.Failure(context, ValidationError("Invalid date: $value", context))
    }
}

@JvmName("asStringDateFromNode")
fun Maybe<JsonNode?>.asStringDate(): Maybe<Instant?> = asString().asStringDate()


fun Instant.fromStringDate(): JsonNode = toString().fromString()