package com.ancientlightstudios.quarkus.kotlin.openapi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.*


@Suppress("unused")
fun String?.asString(context: String): Maybe<String?> = Maybe.Success(context, this)

@Suppress("unused")
fun Maybe<String?>.asString(): Maybe<String?> = this

@Suppress("unused")
fun JsonNode?.asString(context: String): Maybe<String?> = when (this) {
    is ObjectNode, is ArrayNode -> Maybe.Failure(context, ValidationError("is not a string", context))
    null, is NullNode -> Maybe.Success(context, null)
    else -> Maybe.Success(context, this.asText())
}

@Suppress("unused")
@JvmName("asStringFromNode")
fun Maybe<JsonNode?>.asString() = onNotNull { value.asString(context) }

// ------------------------------------------------------------

@Suppress("unused")
fun String?.asFloat(context: String): Maybe<Float?> = this.asMaybe(context, "is not a float") { it.toFloat() }

@Suppress("unused")
fun Maybe<String?>.asFloat(): Maybe<Float?> = this.mapNotNull("is not a float") { it.toFloat() }

@Suppress("unused")
fun JsonNode?.asFloat(context: String): Maybe<Float?> = this.asMaybe(context, "is not a float", String::asFloat)

@Suppress("unused")
@JvmName("asFloatFromNode")
fun Maybe<JsonNode?>.asFloat() = onNotNull { value.asFloat(context) }

// ------------------------------------------------------------

@Suppress("unused")
fun String?.asDouble(context: String): Maybe<Double?> = this.asMaybe(context, "is not a double") { it.toDouble() }

@Suppress("unused")
fun Maybe<String?>.asDouble(): Maybe<Double?> = this.mapNotNull("is not a double") { it.toDouble() }

@Suppress("unused")
fun JsonNode?.asDouble(context: String): Maybe<Double?> = this.asMaybe(context, "is not a double", String::asDouble)

@Suppress("unused")
@JvmName("asDoubleFromNode")
fun Maybe<JsonNode?>.asDouble() = onNotNull { value.asDouble(context) }

// ------------------------------------------------------------

@Suppress("unused")
fun String?.asInt(context: String): Maybe<Int?> = this.asMaybe(context, "is not an int") { it.toInt() }

@Suppress("unused")
fun Maybe<String?>.asInt(): Maybe<Int?> = this.mapNotNull("is not an int") { it.toInt() }

@Suppress("unused")
fun JsonNode?.asInt(context: String): Maybe<Int?> = this.asMaybe(context, "is not an int", String::asInt)

@Suppress("unused")
@JvmName("asIntFromNode")
fun Maybe<JsonNode?>.asInt() = onNotNull { value.asInt(context) }

// ------------------------------------------------------------

@Suppress("unused")
fun String?.asUInt(context: String): Maybe<UInt?> = this.asMaybe(context, "is not an uint") { it.toUInt() }

@Suppress("unused")
fun Maybe<String?>.asUInt(): Maybe<UInt?> = this.mapNotNull("is not an uint") { it.toUInt() }

@Suppress("unused")
fun JsonNode?.asUInt(context: String): Maybe<UInt?> = this.asMaybe(context, "is not an uint", String::asUInt)

@Suppress("unused")
@JvmName("asUIntFromNode")
fun Maybe<JsonNode?>.asUInt() = onNotNull { value.asUInt(context) }

// ------------------------------------------------------------

@Suppress("unused")
fun String?.asULong(context: String): Maybe<ULong?> = this.asMaybe(context, "is not an ulong") { it.toULong() }

@Suppress("unused")
fun Maybe<String?>.asULong(): Maybe<ULong?> = this.mapNotNull("is not an ulong") { it.toULong() }

@Suppress("unused")
fun JsonNode?.asULong(context: String): Maybe<ULong?> = this.asMaybe(context, "is not an ulong", String::asULong)

@Suppress("unused")
@JvmName("asULongFromNode")
fun Maybe<JsonNode?>.asULong() = onNotNull { value.asULong(context) }


// ------------------------------------------------------------

@Suppress("unused")
fun String?.asLong(context: String): Maybe<Long?> = this.asMaybe(context, "is not a long") { it.toLong() }

@Suppress("unused")
fun Maybe<String?>.asLong(): Maybe<Long?> = this.mapNotNull("is not a long") { it.toLong() }

@Suppress("unused")
fun JsonNode?.asLong(context: String): Maybe<Long?> = this.asMaybe(context, "is not a long", String::asLong)

@Suppress("unused")
@JvmName("asLongFromNode")
fun Maybe<JsonNode?>.asLong() = onNotNull { value.asLong(context) }

// ------------------------------------------------------------

@Suppress("unused")
fun String?.asBoolean(context: String): Maybe<Boolean?> = this.asMaybe(context, "is not a boolean") { it.toBoolean() }

@Suppress("unused")
fun Maybe<String?>.asBoolean(): Maybe<Boolean?> = this.mapNotNull("is not a boolean") { it.toBoolean() }

@Suppress("unused")
fun JsonNode?.asBoolean(context: String): Maybe<Boolean?> = when (this) {
    null, is NullNode -> Maybe.Success(context, null)
    is TextNode, is BooleanNode -> asText().asBoolean(context)
    else -> Maybe.Failure(context, ValidationError("is not a boolean", context))
}

@Suppress("unused")
@JvmName("asBooleanFromNode")
fun Maybe<JsonNode?>.asBoolean() = onNotNull { value.asBoolean(context) }

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
