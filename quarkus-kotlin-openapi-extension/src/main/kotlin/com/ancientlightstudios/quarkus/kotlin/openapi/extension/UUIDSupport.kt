package com.ancientlightstudios.quarkus.kotlin.openapi.extension

import com.ancientlightstudios.quarkus.kotlin.openapi.*
import com.fasterxml.jackson.databind.JsonNode
import java.util.*

fun Maybe<String?>.asUUID(): Maybe<UUID?> = onNotNull {
    try {
        success(UUID.fromString(value))
    } catch (e: IllegalArgumentException) {
        failure(ValidationError("Invalid UUID.", context, ErrorKind.Invalid))
    }
}

@JvmName("asUuidFromJson")
fun Maybe<JsonNode?>.asUUID(): Maybe<UUID?> = asString().asUUID()

@JvmName("asStringFromUUID")
fun UUID.asString(): String = toString()

fun UUID.asJson(): JsonNode = asString().asJson()