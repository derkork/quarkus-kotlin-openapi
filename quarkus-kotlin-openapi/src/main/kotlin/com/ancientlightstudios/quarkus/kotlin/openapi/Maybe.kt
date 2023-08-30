package com.ancientlightstudios.quarkus.kotlin.openapi

import com.fasterxml.jackson.databind.ObjectMapper

sealed interface Maybe<T> {
    class Success<T>(val value:T):Maybe<T>
    class Failure<T>(val errors:List<ValidationError>):Maybe<T>
}

fun <T> maybeOf(vararg maybes:Maybe<*>, builder:(Array<*>) -> T):Maybe<T> {
    val errors = mutableListOf<ValidationError>()
    val values = mutableListOf<Any?>()
    for (maybe in maybes) {
        when (maybe) {
            is Maybe.Success -> values.add(maybe.value)
            is Maybe.Failure -> errors.addAll(maybe.errors)
        }
    }
    return if (errors.isEmpty()) {
        Maybe.Success(builder(values.toTypedArray()))
    } else {
        Maybe.Failure(errors)
    }
}

fun String?.asString(path:String):Maybe<String> =
    when(this) {
        null -> Maybe.Failure(listOf(ValidationError(path, "is null")))
        else -> Maybe.Success(this)
    }

fun String?.asFloat(path:String):Maybe<Float> =
    when(this) {
        null -> Maybe.Failure(listOf(ValidationError(path, "is null")))
        else -> try {
            Maybe.Success(this.toFloat())
        } catch (e:Exception) {
            Maybe.Failure(listOf(ValidationError(path, "is not a float")))
        }
    }

fun String?.asDouble(path:String):Maybe<Double> =
    when(this) {
        null -> Maybe.Failure(listOf(ValidationError(path, "is null")))
        else -> try {
            Maybe.Success(this.toDouble())
        } catch (e:Exception) {
            Maybe.Failure(listOf(ValidationError(path, "is not a double")))
        }
    }

fun String?.asInt(path:String):Maybe<Int> =
    when(this) {
        null -> Maybe.Failure(listOf(ValidationError(path, "is null")))
        else -> try {
            Maybe.Success(this.toInt())
        } catch (e:Exception) {
            Maybe.Failure(listOf(ValidationError(path, "is not an int")))
        }
    }

fun String?.asLong(path:String):Maybe<Long> =
    when(this) {
        null -> Maybe.Failure(listOf(ValidationError(path, "is null")))
        else -> try {
            Maybe.Success(this.toLong())
        } catch (e:Exception) {
            Maybe.Failure(listOf(ValidationError(path, "is not a long")))
        }
    }

fun String?.asBoolean(path:String):Maybe<Boolean> =
    when(this) {
        null -> Maybe.Failure(listOf(ValidationError(path, "is null")))
        else -> try {
            Maybe.Success(this.toBoolean())
        } catch (e:Exception) {
            Maybe.Failure(listOf(ValidationError(path, "is not a boolean")))
        }
    }

fun <T> String?.asObject(path:String, type:Class<T>, objectMapper: ObjectMapper) =
    when(this) {
        null -> Maybe.Failure(listOf(ValidationError(path, "is null")))
        else -> try {
            Maybe.Success(objectMapper.readValue(this, type))
        } catch (e:Exception) {
            // TODO: better error message
            Maybe.Failure(listOf(ValidationError(path, "is not a valid json object")))
        }
    }

// TODO: dates
// TODO: optional parameters