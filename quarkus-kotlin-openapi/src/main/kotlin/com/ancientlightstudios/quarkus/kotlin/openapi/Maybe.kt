package com.ancientlightstudios.quarkus.kotlin.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.Maybe.Failure
import com.ancientlightstudios.quarkus.kotlin.openapi.Maybe.Success
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper

sealed class Maybe<T>(val context: String) {
    class Success<T>(context: String, val value: T) : Maybe<T>(context) {
        override fun <O> onSuccess(block: Success<T>.() -> Maybe<O>): Maybe<O> = block(this)

        fun <O> success(value: O) = Success(context, value)
        fun <O> failure(error: ValidationError) = Failure<O>(context, error)
        fun <O> failure(errors: List<ValidationError>) = Failure<O>(context, errors)
    }

    class Failure<T>(context: String, val errors: List<ValidationError>) : Maybe<T>(context) {
        constructor(context: String, error: ValidationError) : this(context, listOf(error))

        @Suppress("UNCHECKED_CAST")
        override fun <O> onSuccess(block: Success<T>.() -> Maybe<O>): Maybe<O> = this as Maybe<O>
    }

    /**
     * executes the given block and returns its result if this maybe is a [Success] or just returns the [Failure]
     */
    abstract fun <O> onSuccess(block: Success<T>.() -> Maybe<O>): Maybe<O>
}

/**
 * combines the given maybes. Returns a [Success] with the result of the builder if all given maybes are
 * [Success] too, a [Failure] otherwise.
 */
@Suppress("unused")
fun <T> maybeOf(context: String, vararg maybes: Maybe<*>, builder: (Array<*>) -> T): Maybe<T> {
    val errors = mutableListOf<ValidationError>()
    val values = mutableListOf<Any?>()
    for (maybe in maybes) {
        when (maybe) {
            is Success -> values.add(maybe.value)
            is Failure -> errors.addAll(maybe.errors)
        }
    }
    return if (errors.isEmpty()) {
        Success(context, builder(values.toTypedArray()))
    } else {
        Failure(context, errors)
    }
}

/**
 * wraps this value into a [Success]
 */
@Suppress("unused")
fun <T> T?.asMaybe(context: String): Maybe<T?> = Success(context, this)

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
fun String?.asString(context: String): Maybe<String?> = Success(context, this)
@Suppress("unused")
fun Maybe<String?>.asString(): Maybe<String?> = this

@Suppress("unused")
fun String?.asFloat(context: String): Maybe<Float?> = this.asMaybe(context, "is not a float") { it.toFloat() }
@Suppress("unused")
fun Maybe<String?>.asFloat(): Maybe<Float?> = this.mapNotNull("is not a float") { it.toFloat() }

@Suppress("unused")
fun String?.asDouble(context: String): Maybe<Double?> = this.asMaybe(context, "is not a double") { it.toDouble() }
@Suppress("unused")
fun Maybe<String?>.asDouble(): Maybe<Double?> = this.mapNotNull("is not a double") { it.toDouble() }

@Suppress("unused")
fun String?.asInt(context: String): Maybe<Int?> = this.asMaybe(context, "is not a int") { it.toInt() }
@Suppress("unused")
fun Maybe<String?>.asInt(): Maybe<Int?> = this.mapNotNull("is not a int") { it.toInt() }

@Suppress("unused")
fun String?.asLong(context: String): Maybe<Long?> = this.asMaybe(context, "is not a long") { it.toLong() }
@Suppress("unused")
fun Maybe<String?>.asLong(): Maybe<Long?> = this.mapNotNull("is not a long") { it.toLong() }

@Suppress("unused")
fun String?.asBoolean(context: String): Maybe<Boolean?> = this.asMaybe(context, "is not a boolean") { it.toBoolean() }
@Suppress("unused")
fun Maybe<String?>.asBoolean(): Maybe<Boolean?> = this.mapNotNull("is not a boolean") { it.toBoolean() }

@Suppress("unused")
fun <T> String?.asObject(context: String, type: Class<T>, objectMapper: ObjectMapper): Maybe<T?> =
    this.asMaybe(context, "is not a valid json object") { objectMapper.readValue(this, type) }

@Suppress("unused")
inline fun <reified T> String?.asList(context: String, objectMapper: ObjectMapper): Maybe<T?> =
    this.asMaybe(context, "is not a valid json array") { objectMapper.readValue(this, object : TypeReference<T>() {}) }


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
