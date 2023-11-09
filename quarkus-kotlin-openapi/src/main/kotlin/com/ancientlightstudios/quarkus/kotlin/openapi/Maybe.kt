package com.ancientlightstudios.quarkus.kotlin.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.Maybe.Failure
import com.ancientlightstudios.quarkus.kotlin.openapi.Maybe.Success
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.NumericNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode

sealed class Maybe<T>(val context: String) {
    class Success<T>(context: String, val value: T) : Maybe<T>(context) {
        override fun <O> onSuccess(block: Success<T>.() -> Maybe<O>): Maybe<O> = block(this)

        fun <O> success(value: O) = Success(context, value)
        fun <O> failure(error: ValidationError) = Failure<O>(context, error)
        fun <O> failure(errors: List<ValidationError>) = Failure<O>(context, errors)
        override fun validValueOrNull() = value
    }

    class Failure<T>(context: String, val errors: List<ValidationError>) : Maybe<T>(context) {
        constructor(context: String, error: ValidationError) : this(context, listOf(error))

        @Suppress("UNCHECKED_CAST")
        override fun <O> onSuccess(block: Success<T>.() -> Maybe<O>): Maybe<O> = this as Maybe<O>
        override fun validValueOrNull() = null
    }

    /**
     * executes the given block and returns its result if this maybe is a [Success] or just returns the [Failure]
     */
    abstract fun <O> onSuccess(block: Success<T>.() -> Maybe<O>): Maybe<O>
    abstract fun validValueOrNull(): T?

}

/**
 * combines the given maybes. Returns a [Success] with the result of the builder if all given maybes are
 * [Success] too, a [Failure] otherwise.
 */
@Suppress("unused")
fun <T> maybeAllOf(context: String, vararg maybes: Maybe<*>, builder: () -> T): Maybe<T> {
    val errors = maybes.filterIsInstance<Failure<*>>().flatMap { it.errors }
    return if (errors.isEmpty()) {
        Success(context, builder())
    } else {
        Failure(context, errors)
    }
}

/**
 * combines the given maybes. Returns a [Success] with the result of the builder if at least one given maybe is
 * a [Success], a [Failure] otherwise.
 */
@Suppress("unused")
fun <T> maybeAnyOf(context: String, vararg maybes: Maybe<*>, builder: () -> T): Maybe<T> {
    return if (maybes.any { it is Success }) {
        Success(context, builder())
    } else {
        val errors = maybes.filterIsInstance<Failure<*>>().flatMap { it.errors }
        Failure(context, errors)
    }
}

/**
 * combines the given maybes. Returns a [Success] with the result of the builder if exactly one given maybe
 * is a [Success], a [Failure] otherwise.
 */
@Suppress("unused")
fun <T> maybeOneOf(context: String, vararg maybes: Maybe<*>, builder: () -> T): Maybe<T> {
    val count = maybes.count { it is Success }
    return if (count == 1) {
        Success(context, builder())
    } else if (count > 1) {
        Failure(context, ValidationError("is ambiguous", context))
    } else {
        val errors = maybes.filterIsInstance<Failure<*>>().flatMap { it.errors }
        Failure(context, errors)
    }
}

/**
 * wraps this value into a [Success]
 */
@Suppress("unused")
fun <T> T?.asMaybe(context: String): Maybe<T?> = Success(context, this)

/**
 * wraps the requested property or null into a [Success]
 */
fun JsonNode?.asMaybe(property: String, context: String): Maybe<JsonNode?> =
    Success(context, this?.get(property))

/**
 * wraps the result of the block into a [Success]. Returns a [Failure] if an exception occurred
 */
inline fun <T> String?.asMaybe(context: String, validationMessage: String, block: (String) -> T): Maybe<T?> =
    try {
        Success(context, this?.let(block))
    } catch (e: Exception) {
        Failure(context, ValidationError(validationMessage, context))
    }

@Suppress("unused")
inline fun <T> JsonNode?.asMaybe(
    context: String,
    validationMessage: String,
    block: String.(String) -> Maybe<T?>
): Maybe<T?> = when (this) {
    null, is NullNode -> Success(context, null)
    is TextNode, is NumericNode -> asText().block(context)
    else -> Failure(context, ValidationError(validationMessage, context))
}

// ------------------------------------------------------------

@Suppress("unused")
fun String?.asString(context: String): Maybe<String?> = Success(context, this)

@Suppress("unused")
fun Maybe<String?>.asString(): Maybe<String?> = this

@Suppress("unused")
fun JsonNode?.asString(context: String): Maybe<String?> = when (this) {
    is ObjectNode, is ArrayNode -> Failure(context, ValidationError("is not a string", context))
    null, is NullNode -> Success(context, null)
    else -> Success(context, this.asText())
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
    null, is NullNode -> Success(context, null)
    is TextNode, is BooleanNode -> asText().asBoolean(context)
    else -> Failure(context, ValidationError("is not a boolean", context))
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
        else -> Failure(context, ValidationError("is not a valid json object", context))
    }
}

@Suppress("unused")
fun Maybe<JsonNode?>.asList(): Maybe<List<JsonNode?>?> = onNotNull {
    when (this.value) {
        is NullNode -> success(null)
        is ArrayNode -> success(this.value.toList())
        else -> Failure(context, ValidationError("is not a valid json object", context))
    }
}

/**
 * executes the given block and returns its result if this maybe is a [Success]. Returns a [Failure] if an
 * exception occurred or this maybe already is a [Failure]
 */
@Suppress("unused")
inline fun <I, O> Maybe<I?>.map(
    validationMessage: String = "is not a valid value",
    crossinline block: (I?) -> O?
): Maybe<O?> =
    onSuccess {
        try {
            success(block(value))
        } catch (e: Exception) {
            failure(ValidationError(validationMessage, context))
        }
    }

/**
 * executes the given block and returns its result if this maybe is a [Success] and has a non-null value. Returns a
 * [Failure] if an exception occurred or this maybe already is a [Failure]
 */
@Suppress("unused")
inline fun <I, O> Maybe<I?>.mapNotNull(
    validationMessage: String = "is not a valid value",
    crossinline block: (I) -> O?
): Maybe<O?> =
    onNotNull {
        try {
            success(block(value))
        } catch (e: Exception) {
            failure(ValidationError(validationMessage, context))
        }
    }

/**
 * executes the given block if this maybe is a [Failure] or returns the value of the [Success]
 */
@Suppress("unused")
inline fun <T> Maybe<T>.validOrElse(block: (List<ValidationError>) -> Nothing): T {
    if (this is Failure) {
        block(errors)
    }
    return (this as Success).value
}
