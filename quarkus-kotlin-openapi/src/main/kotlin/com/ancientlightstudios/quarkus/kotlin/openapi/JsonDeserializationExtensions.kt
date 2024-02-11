package com.ancientlightstudios.quarkus.kotlin.openapi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode

fun Maybe<String?>.asJson(objectMapper: ObjectMapper): Maybe<JsonNode?> = onNotNull {
    try {
        success(objectMapper.readValue(value, JsonNode::class.java))
    } catch (_: Exception) {
        failure(ValidationError("is not valid json", context))
    }
}

@Suppress("unused")
fun Maybe<JsonNode?>.asObject(): Maybe<JsonNode?> = onNotNull {
    when (this.value) {
        is NullNode -> success(null)
        is ObjectNode -> this@asObject
        else -> Maybe.Failure(context, ValidationError("is not a valid json object", context))
    }
}

@Suppress("unused")
fun Maybe<JsonNode?>.asList(): Maybe<List<JsonNode?>?> = onNotNull {
    when (this.value) {
        is NullNode -> success(null)
        is ArrayNode -> success(this.value.toList())
        else -> Maybe.Failure(context, ValidationError("is not a valid json object", context))
    }
}

@Suppress("unused")
@JvmName("asStringFromJson")
fun Maybe<JsonNode?>.asString() = onNotNull {
    when (value) {
        is NullNode -> Maybe.Success(context, null)
        is ObjectNode, is ArrayNode -> Maybe.Failure(context, ValidationError("is not a string", context))
        else -> Maybe.Success(context, value.asText())
    }
}

@Suppress("unused")
@JvmName("asIntFromJson")
fun Maybe<JsonNode?>.asInt() = asString().asInt()

@Suppress("unused")
@JvmName("asUIntFromJson")
fun Maybe<JsonNode?>.asUInt() = asString().asUInt()

@Suppress("unused")
@JvmName("asLongFromJson")
fun Maybe<JsonNode?>.asLong() = asString().asLong()

@Suppress("unused")
@JvmName("asULongFromJson")
fun Maybe<JsonNode?>.asULong() = asString().asULong()

@Suppress("unused")
@JvmName("asFloatFromJson")
fun Maybe<JsonNode?>.asFloat() = asString().asFloat()

@Suppress("unused")
@JvmName("asDoubleFromJson")
fun Maybe<JsonNode?>.asDouble() = asString().asDouble()


@Suppress("unused")
@JvmName("asBooleanFromJson")
fun Maybe<JsonNode?>.asBoolean() = asString().asBoolean()
