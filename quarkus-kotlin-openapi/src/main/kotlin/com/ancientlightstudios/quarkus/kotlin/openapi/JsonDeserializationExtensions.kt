package com.ancientlightstudios.quarkus.kotlin.openapi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode

fun Maybe<String?>.asJson(objectMapper: ObjectMapper): Maybe<JsonNode?> = onNotNull {
    try {
        if (value.isEmpty()) {
            success(null)
        } else {
            success(objectMapper.readValue(value, JsonNode::class.java))
        }
    } catch (_: Exception) {
        failure(ValidationError("is not valid json", context, ErrorKind.Incompatible))
    }
}

@Suppress("unused")
fun Maybe<out JsonNode?>.asObject(): Maybe<JsonNode?> = onNotNull {
    when (this.value) {
        is NullNode -> success(null)
        is ObjectNode -> this@asObject as Maybe<JsonNode?>
        else -> Maybe.Failure(context, ValidationError("is not a valid json object", context, ErrorKind.Incompatible))
    }
}

@Suppress("unused")
fun JsonNode?.findProperty(name: String, context: String): Maybe<JsonNode?> = Maybe.Success(context, this?.get(name))

// returns the amount of properties in the json node. only properties from the known list will be considered.
// if a property has a default value but is not declared in the node it will still affect the counter
fun JsonNode.countKnownProperties(knownProperties: List<String>, propertiesWithDefault: List<String>): Int {
    // fieldNames() just return an iterator which requires an extra step to convert it into a set or list
    val declaredPropertyNames = properties().map { it.key }.toMutableSet()
    declaredPropertyNames.addAll(propertiesWithDefault)
    return knownProperties.count { it in declaredPropertyNames }
}

// returns the amount of properties in the json node. if a property has a default value but is not declared in the
// node it will still affect the counter
fun JsonNode.countAllProperties(propertiesWithDefault: List<String>): Int {
    val declaredPropertyNames = properties().map { it.key }.toMutableSet()
    declaredPropertyNames.addAll(propertiesWithDefault)
    return declaredPropertyNames.size
}


@Suppress("unused")
fun Maybe<out JsonNode?>.asList(): Maybe<List<JsonNode?>?> = onNotNull {
    when (this.value) {
        is NullNode -> success(null)
        is ArrayNode -> success(this.value.toList())
        else -> Maybe.Failure(context, ValidationError("is not a valid json array", context, ErrorKind.Incompatible))
    }
}

@Suppress("unused")
@JvmName("asStringFromJson")
fun Maybe<JsonNode?>.asString() = onNotNull {
    when (value) {
        is NullNode -> Maybe.Success(context, null)
        is ObjectNode, is ArrayNode -> Maybe.Failure(context, ValidationError("is not a string", context, ErrorKind.Incompatible))
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
@JvmName("asBigIntegerFromJson")
fun Maybe<JsonNode?>.asBigInteger() = asString().asBigInteger()

@Suppress("unused")
@JvmName("asFloatFromJson")
fun Maybe<JsonNode?>.asFloat() = asString().asFloat()

@Suppress("unused")
@JvmName("asDoubleFromJson")
fun Maybe<JsonNode?>.asDouble() = asString().asDouble()

@Suppress("unused")
@JvmName("asBigDecimalFromJson")
fun Maybe<JsonNode?>.asBigDecimal() = asString().asBigDecimal()

@Suppress("unused")
@JvmName("asBooleanFromJson")
fun Maybe<JsonNode?>.asBoolean() = asString().asBoolean()

@Suppress("unused")
@JvmName("asByteArrayFromJson")
fun Maybe<JsonNode?>.asByteArray() = asString().asByteArray()

