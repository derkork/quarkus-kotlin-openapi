package com.ancientlightstudios.quarkus.kotlin.openapi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

fun String?.parseAsJson(context: String, objectMapper: ObjectMapper): Maybe<JsonNode?> =
    try {
        this?.let { objectMapper.readValue(it, JsonNode::class.java) }.asMaybe(context)
    } catch (_: Exception) {
        Maybe.Failure(context, ValidationError("is not valid json", context))
    }
