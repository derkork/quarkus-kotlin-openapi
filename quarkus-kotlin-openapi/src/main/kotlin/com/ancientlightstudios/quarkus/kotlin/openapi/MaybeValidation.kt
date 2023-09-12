package com.ancientlightstudios.quarkus.kotlin.openapi


inline fun <I, O> Maybe<I>.skipOnFailure(block: Maybe.Success<I>.() -> Maybe<O>): Maybe<O> = when (this) {
    is Maybe.Failure -> Maybe.Failure(context, errors)
    is Maybe.Success -> block()
}

inline fun <I, O> Maybe.Success<I?>.succeedWhenNullElse(block: Maybe.Success<I & Any>.() -> Maybe<O?>): Maybe<O?> =
    if (value == null) {
        Maybe.Success(context, null)
    } else {
        Maybe.Success(context, value).block()
    }

fun <U, S> Maybe<List<U?>?>.validated(block: (value: Maybe<U?>) -> Maybe<S?>): Maybe<List<S?>?> =
    skipOnFailure {
        succeedWhenNullElse {
            try {
                val validated = mutableListOf<S?>()
                val errors = mutableListOf<ValidationError>()
                value.forEachIndexed { index, item ->
                    when (val validatedItem = block(item.maybeOf(this.context + "[" + index + "]"))) {
                        is Maybe.Failure -> errors.addAll(validatedItem.errors)
                        is Maybe.Success -> validated.add(validatedItem.value)
                    }
                }

                if (errors.isNotEmpty()) {
                    Maybe.Failure(context, errors)
                } else {
                    Maybe.Success(context, validated)
                }
            } catch (e: Exception) {
                Maybe.Failure(context, ValidationError(e.message ?: "is not a valid value"))
            }
        }
    }

fun <T> Maybe<T?>.required(): Maybe<T?> =
    skipOnFailure {
        if (value != null) {
            Maybe.Success(context, value)
        } else {
            Maybe.Failure(context, ValidationError("is required"))
        }
    }

fun <T : Number> Maybe<T?>.minimum(minimum: Number, exclusive: Boolean): Maybe<T?> =
    skipOnFailure {
        succeedWhenNullElse {
            if (value.toDouble() > minimum.toDouble() || (exclusive && value.toDouble() == minimum.toDouble())) {
                Maybe.Success(context, value)
            } else {
                Maybe.Failure(context, ValidationError("must be greater than $minimum"))
            }
        }
    }

