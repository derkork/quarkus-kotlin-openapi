package com.ancientlightstudios.quarkus.kotlin.openapi

/**
 * checks that the value of the maybe is not null
 * @return the given maybe or a [Maybe.Failure] if the value was null
 */
fun <T> Maybe<T?>.required(): Maybe<T?> =
    onSuccess {
        if (value != null) {
            this
        } else {
            failure(ValidationError("is required"))
        }
    }

/**
 * executes the given block if this maybe has a value which is not null.
 */
inline fun <I, O> Maybe<I?>.onNotNull(crossinline block: Maybe.Success<I & Any>.() -> Maybe<O?>): Maybe<O?> =
    onSuccess {
        if (value != null) {
            success(value).block()
        } else {
            success(null)
        }
    }

fun <U, S> Maybe<List<U?>?>.validated(block: (value: Maybe<U?>) -> Maybe<S?>): Maybe<List<S?>?> =
    onNotNull {
        try {
            val validated = mutableListOf<S?>()
            val errors = mutableListOf<ValidationError>()
            value.forEachIndexed { index, item ->
                when (val validatedItem = block(item.asMaybe(this.context + "[" + index + "]"))) {
                    is Maybe.Failure -> errors.addAll(validatedItem.errors)
                    is Maybe.Success -> validated.add(validatedItem.value)
                }
            }

            if (errors.isNotEmpty()) {
                failure(errors)
            } else {
                success(validated)
            }
        } catch (e: Exception) {
            failure(ValidationError(e.message ?: "is not a valid value"))
        }
    }

// TODO: this is just a workaround to fix the generated code in case there is a list of strings. should be replaced with a proper validation implementation
fun Maybe<String?>.validated(): Maybe<String?> = this