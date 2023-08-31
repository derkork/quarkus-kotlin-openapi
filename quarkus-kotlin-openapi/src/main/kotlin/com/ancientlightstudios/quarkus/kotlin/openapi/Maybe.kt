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

fun <T> maybeOf(context: String, vararg maybes: Maybe<*>, builder: (Array<*>) -> T): Maybe<T> {
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


private inline fun <T> String?.asMaybe(context: String, validationMessage: String, block: (String) -> T): Maybe<T?> =
    when (this) {
        null -> Maybe.Success(context,null)
        else -> try {
            Maybe.Success(context,block(this))
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


fun <T> String?.asEnum(context: String, type: Class<T>, objectMapper: ObjectMapper): Maybe<T?> =
    this.asMaybe(context, "is not a valid enum value") { objectMapper.convertValue(this, type) }

fun <T> Maybe<T>.validated(block: (T, validationErrors: MutableList<ValidationError>) -> Unit): Maybe<T> {
    return when (this) {
        is Maybe.Failure -> this
        is Maybe.Success -> {
            val validationErrors = mutableListOf<ValidationError>()
            block(this.value, validationErrors)
            if (validationErrors.isEmpty()) {
                this
            } else {
                this.failure(validationErrors)
            }
        }
    }
}

fun <T> Maybe<T?>.required(): Maybe<T?> {
    return when (this) {
        is Maybe.Failure -> this
        is Maybe.Success -> if (value != null) {
            this
        } else {
            this.failure(ValidationError("is required"))
        }
    }
}

// TODO: dates
// TODO: optional parameters