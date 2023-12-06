package com.ancientlightstudios.quarkus.kotlin.openapi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode

fun String?.parseAsJson(context: String, objectMapper: ObjectMapper): Maybe<JsonNode?> =
    try {
        if (this.isNullOrBlank()) {
            Maybe.Success(context, NullNode.instance)
        }
        else {
            objectMapper.readValue(this, JsonNode::class.java).asMaybe(context)
        }
    } catch (_: Exception) {
        Maybe.Failure(context, ValidationError("is not valid json", context))
    }
