package com.ancientlightstudios.quarkus.kotlin.openapi

import com.fasterxml.jackson.databind.ObjectMapper

sealed class Maybe<T>(val context: String) {
    class Success<T>(context: String, val value: T) : Maybe<T>(context) {
        fun failure(error: ValidationError) = Failure<T>(context, error)
        fun failure(errors: List<ValidationError>) = Failure<T>(context, errors)
    }

    class Failure<T>(context: String, val errors: List<ValidationError>) : Maybe<T>(context) {
        constructor(context: String, error: ValidationError) : this(context, listOf(error))
    }
}

fun <T> maybeOf(context: String, vararg maybes: Maybe<*>, builder: (Array<*>) -> T): Maybe<T?> {
    val errors = mutableListOf<ValidationError>()
    val values = mutableListOf<Any?>()
    for (maybe in maybes) {
        when (maybe) {
            is Maybe.Success -> values.add(maybe.value)
            is Maybe.Failure -> errors.addAll(maybe.errors)
        }
    }
    return if (errors.isEmpty()) {
        Maybe.Success(context, builder(values.toTypedArray()))
    } else {
        Maybe.Failure(context, errors)
    }
}

fun <T> T?.maybeOf(context: String): Maybe<T?> = Maybe.Success(context, this)
fun <T> failedMaybeOf(context:String, errorMessage:String) : Maybe<T?> = Maybe.Failure(context, ValidationError(errorMessage))

private inline fun <T> String?.asMaybe(context: String, validationMessage: String, block: (String) -> T): Maybe<T?> =
    when (this) {
        null -> Maybe.Success(context, null)
        else -> try {
            Maybe.Success(context, block(this))
        } catch (e: Exception) {
            Maybe.Failure(context, ValidationError(validationMessage))
        }
    }

fun String?.asString(context: String): Maybe<String?> = Maybe.Success(context, this)
fun String?.asFloat(context: String): Maybe<Float?> = this.asMaybe(context, "is not a float") { it.toFloat() }
fun String?.asDouble(context: String): Maybe<Double?> = this.asMaybe(context, "is not a double") { it.toDouble() }
fun String?.asInt(context: String): Maybe<Int?> = this.asMaybe(context, "is not a int") { it.toInt() }
fun String?.asLong(context: String): Maybe<Long?> = this.asMaybe(context, "is not a long") { it.toLong() }
fun String?.asBoolean(context: String): Maybe<Boolean?> = this.asMaybe(context, "is not a boolean") { it.toBoolean() }

fun <T> String?.asObject(context: String, type: Class<T>, objectMapper: ObjectMapper): Maybe<T?> =
    this.asMaybe(context, "is not a valid json object") { objectMapper.readValue(this, type) }



fun <I, O> Maybe<I>.map(block: (I) -> O): Maybe<O> {
    return when (this) {
        is Maybe.Failure -> Maybe.Failure(context, errors)
        is Maybe.Success -> {
            try {
                Maybe.Success(context, block(value))
            } catch (e: Exception) {
                Maybe.Failure(context, ValidationError("is not a valid value"))
            }
        }
    }
}

inline fun <T> Maybe<T>.validOrElse(block: (List<ValidationError>) -> Nothing): T {
    if (this is Maybe.Failure) {
        block(errors)
    }
    return (this as Maybe.Success).value
}

fun <T> Maybe<T?>.forceNotNull() : Maybe<T> = this.map { it!! }

fun <T> maybeCast(value: Any?): T = value as T
