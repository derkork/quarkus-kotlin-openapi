package com.ancientlightstudios.quarkus.kotlin.openapi

open class Validator {

    private val _validationErrors = mutableListOf<ValidationError>()

    val validationErrors: List<ValidationError>
        get() = _validationErrors

    protected fun reportError(error: ValidationError) {
        _validationErrors.add(error)
    }

}

class StringValidator : Validator() {
    fun String.minLength(min: Int) {

    }

    fun String.maxLength(max: Int) {

    }

    fun String.pattern(pattern: Regex) {

    }

}

class NumberValidator<T : Number> : Validator() {
    fun T.minimum(min: T) {

    }

    fun T.maximum(max: T) {

    }

}

class ListValidator : Validator() {
    fun List<*>.minSize(min: Int) {

    }

    fun List<*>.maxSize(min: Int) {

    }
}

fun Maybe<String?>.validateString(block: StringValidator.(String) -> Unit): Maybe<String?> =
    onNotNull {
        val errors = StringValidator().apply { this.block(value) }.validationErrors
        if (errors.isEmpty()) {
            this@validateString
        } else {
            failure(errors)
        }
    }

fun <T : Number> Maybe<T?>.validateNumber(block: NumberValidator<T>.(T) -> Unit): Maybe<T?> =
    onNotNull {
        val errors = NumberValidator<T>().apply { this.block(value) }.validationErrors
        if (errors.isEmpty()) {
            this@validateNumber
        } else {
            failure(errors)
        }
    }

fun <I, O> Maybe<I?>.validateUnsafe(block: I.(String) -> Maybe<O>): Maybe<O?> =
    onNotNull {
        @Suppress("UNCHECKED_CAST")
        value.block(this.context) as Maybe<O?>
    }

fun <I> Maybe<List<I?>?>.validateList(block: ListValidator.(List<I?>) -> Unit): Maybe<List<I?>?> =
    onNotNull {
        val errors = ListValidator().apply { this.block(value) }.validationErrors
        if (errors.isEmpty()) {
            this@validateList
        } else {
            failure(errors)
        }
    }

fun <I, O> Maybe<List<I?>?>.validateListItems(block: (Maybe.Success<I?>) -> Maybe<O>): Maybe<List<O>?> =
    onNotNull {
        try {
            val validated = mutableListOf<O>()
            val errors = mutableListOf<ValidationError>()
            value.forEachIndexed { index, item ->
                when (val validatedItem = block(Maybe.Success("$context[$index]", item))) {
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

/**
 * checks that the value of the maybe is not null
 * @return the given maybe or a [Maybe.Failure] if the value was null
 */
fun <T> Maybe<T?>.required(): Maybe<T> =
    onSuccess {
        if (value != null) {
            @Suppress("UNCHECKED_CAST")
            this as Maybe<T>
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
            @Suppress("UNCHECKED_CAST")
            (this as Maybe.Success<I & Any>).block()
        } else {
            @Suppress("UNCHECKED_CAST")
            this as Maybe<O?>
        }
    }
