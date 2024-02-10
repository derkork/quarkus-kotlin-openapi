package com.ancientlightstudios.quarkus.kotlin.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.Maybe.Failure
import com.ancientlightstudios.quarkus.kotlin.openapi.Maybe.Success
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.NumericNode
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
 * wraps the requested property or null into a [Success]
 */
fun JsonNode?.asMaybe(property: String, context: String): Maybe<JsonNode?> =
    Success(context, this?.get(property))

///**
// * wraps the result of the block into a [Success]. Returns a [Failure] if an exception occurred
// */
//inline fun <T> String?.asMaybe(context: String, validationMessage: String, block: (String) -> T): Maybe<T?> =
//    try {
//        Success(context, this?.let(block))
//    } catch (e: Exception) {
//        Failure(context, ValidationError(validationMessage, context))
//    }

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
