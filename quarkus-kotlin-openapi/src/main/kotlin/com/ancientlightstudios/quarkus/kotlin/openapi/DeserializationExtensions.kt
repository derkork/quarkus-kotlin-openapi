package com.ancientlightstudios.quarkus.kotlin.openapi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.*



@Suppress("unused")
fun Maybe<String?>.asString(): Maybe<String?> = this

@Suppress("unused")
@JvmName("asStringFromNode")
fun Maybe<JsonNode?>.asString() = onNotNull {
    when (value) {
        is NullNode -> Maybe.Success(context, null)
        is ObjectNode, is ArrayNode -> Maybe.Failure(context, ValidationError("is not a string", context))
        else -> Maybe.Success(context, value.asText())
    }
}

// ------------------------------------------------------------

@Suppress("unused")
fun Maybe<String?>.asFloat(): Maybe<Float?> = this.mapNotNull("is not a float") { it.toFloat() }

@Suppress("unused")
@JvmName("asFloatFromNode")
fun Maybe<JsonNode?>.asFloat() = asString().asFloat()

// ------------------------------------------------------------

@Suppress("unused")
fun Maybe<String?>.asDouble(): Maybe<Double?> = this.mapNotNull("is not a double") { it.toDouble() }

@Suppress("unused")
@JvmName("asDoubleFromNode")
fun Maybe<JsonNode?>.asDouble() = asString().asDouble()

// ------------------------------------------------------------

@Suppress("unused")
fun Maybe<String?>.asInt(): Maybe<Int?> = this.mapNotNull("is not an int") { it.toInt() }

@Suppress("unused")
@JvmName("asIntFromNode")
fun Maybe<JsonNode?>.asInt() = asString().asInt()

// ------------------------------------------------------------

@Suppress("unused")
fun Maybe<String?>.asUInt(): Maybe<UInt?> = this.mapNotNull("is not an uint") { it.toUInt() }

@Suppress("unused")
@JvmName("asUIntFromNode")
fun Maybe<JsonNode?>.asUInt() = asString().asUInt()

// ------------------------------------------------------------

@Suppress("unused")
fun Maybe<String?>.asULong(): Maybe<ULong?> = this.mapNotNull("is not an ulong") { it.toULong() }

@Suppress("unused")
@JvmName("asULongFromNode")
fun Maybe<JsonNode?>.asULong() = asString().asULong()


// ------------------------------------------------------------

@Suppress("unused")
fun Maybe<String?>.asLong(): Maybe<Long?> = this.mapNotNull("is not a long") { it.toLong() }

@Suppress("unused")
@JvmName("asLongFromNode")
fun Maybe<JsonNode?>.asLong() = asString().asLong()

// ------------------------------------------------------------

@Suppress("unused")
fun Maybe<String?>.asBoolean(): Maybe<Boolean?> = this.mapNotNull("is not a boolean") { it.toBoolean() }

@Suppress("unused")
@JvmName("asBooleanFromNode")
fun Maybe<JsonNode?>.asBoolean() = asString().asBoolean()

// ------------------------------------------------------------

@Suppress("unused")
fun <T> Maybe<JsonNode?>.asObject(block: (JsonNode) -> T): Maybe<T?> = onNotNull {
    when (this.value) {
        is NullNode -> success(null)
        is ObjectNode -> success(block(value))
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
