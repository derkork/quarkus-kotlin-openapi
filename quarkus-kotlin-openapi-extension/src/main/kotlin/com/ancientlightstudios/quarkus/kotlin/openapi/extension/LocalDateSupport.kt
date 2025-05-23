package com.ancientlightstudios.quarkus.kotlin.openapi.extension

import com.ancientlightstudios.quarkus.kotlin.openapi.*
import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDate


fun Maybe<String?>.asLocalDate(): Maybe<LocalDate?> = onNotNull {
    try {
        success(LocalDate.parse(value))
    } catch (e: Exception) {
        failure(ValidationError("Invalid date.", context, ErrorKind.Invalid))
    }
}

@JvmName("asLocalDateFromJson")
fun Maybe<JsonNode?>.asLocalDate(): Maybe<LocalDate?> = asString().asLocalDate()

@JvmName("asStringFromLocalDate")
fun LocalDate.asString(): String = toString()

fun LocalDate.asJson(): JsonNode = asString().asJson()