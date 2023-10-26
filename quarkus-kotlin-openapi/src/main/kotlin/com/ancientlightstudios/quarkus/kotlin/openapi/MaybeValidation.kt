package com.ancientlightstudios.quarkus.kotlin.openapi

open class Validator(private val context: String) {

    private val _validationErrors = mutableListOf<ValidationError>()

    val validationErrors: List<ValidationError>
        get() = _validationErrors

    protected fun reportError(reason: String, path: String) {
        _validationErrors.add(ValidationError(reason, "${context}.$path"))
    }

    protected fun reportError(reason: String) {
        _validationErrors.add(ValidationError(reason, context))
    }

}

@Suppress("unused")
class DefaultValidator(context: String) : Validator(context) {

    fun fail(reason: String) = reportError(reason)

    fun fail(reason: String, path: String) = reportError(reason, path)

}

@Suppress("unused")
class StringValidator(context: String) : Validator(context) {

    fun String.minLength(min: Int) {
        if (length < min) {
            reportError("minimum length of $min expected, but is $length")
        }
    }

    fun String.maxLength(max: Int) {
        if (length > max) {
            reportError("maximum length of $max expected, but is $length")
        }
    }

    fun String.pattern(pattern: String) {
        val regex = PatternCache.compilePattern(pattern)
        if (!regex.matches(this)) {
            reportError("doesn't match pattern '$pattern'")
        }
    }

}

@Suppress("unused")
class NumberValidator<T : Number>(context: String) : Validator(context) {

    fun T.minimum(min: T, exclusive: Boolean) {
        if (exclusive) {
            if (this.compare(min) <= 0) {
                reportError("exclusive minimum of $min expected, but is $this")
            }
        } else {
            if (this.compare(min) < 0) {
                reportError(" minimum of $min expected, but is $this")
            }
        }
    }

    fun T.maximum(max: T, exclusive: Boolean) {
        if (exclusive) {
            if (this.compare(max) >= 0) {
                reportError("exclusive maximum of $max expected, but is $this")
            }
        } else {
            if (this.compare(max) > 0) {
                reportError(" maximum of $max expected, but is $this")
            }
        }
    }

    private fun T.compare(other: T) = when (this) {
        is Double, is Float -> this.toDouble().compareTo(other.toDouble())
        else -> this.toLong().compareTo(other.toLong())
    }

}

@Suppress("unused")
class ListValidator(context: String) : Validator(context) {

    fun List<*>.minSize(min: Int) {
        if (size < min) {
            reportError("minimum size of $min expected, but is $size")
        }
    }

    fun List<*>.maxSize(max: Int) {
        if (size < max) {
            reportError("maximum size of $max expected, but is $size")
        }
    }
}

@Suppress("unused")
fun <T> Maybe<T?>.validate(block: DefaultValidator.(T) -> Unit): Maybe<T?> =
    onNotNull {
        val errors = DefaultValidator(context).apply { this.block(value) }.validationErrors
        if (errors.isEmpty()) {
            this@validate
        } else {
            failure(errors)
        }
    }

@Suppress("unused")
fun Maybe<String?>.validateString(block: StringValidator.(String) -> Unit): Maybe<String?> =
    onNotNull {
        val errors = StringValidator(context).apply { this.block(value) }.validationErrors
        if (errors.isEmpty()) {
            this@validateString
        } else {
            failure(errors)
        }
    }

@JvmName("validateStringWithWrapper")
@Suppress("unused")
fun <W: TypeWrapper<String>> Maybe<W?>.validateString(block: StringValidator.(String) -> Unit): Maybe<W?> =
    onNotNull {
        val errors = StringValidator(context).apply { this.block(value.value) }.validationErrors
        if (errors.isEmpty()) {
            this@validateString
        } else {
            failure(errors)
        }
    }


@Suppress("unused")
fun <T : Number> Maybe<T?>.validateNumber(block: NumberValidator<T>.(T) -> Unit): Maybe<T?> =
    onNotNull {
        val errors = NumberValidator<T>(context).apply { this.block(value) }.validationErrors
        if (errors.isEmpty()) {
            this@validateNumber
        } else {
            failure(errors)
        }
    }

@JvmName("validateNumberWithWrapper")
@Suppress("unused")
fun <T : Number, W : TypeWrapper<T>> Maybe<W?>.validateNumber(block: NumberValidator<T>.(T) -> Unit): Maybe<W?> =
    onNotNull {
        val errors = NumberValidator<T>(context).apply { this.block(value.value) }.validationErrors
        if (errors.isEmpty()) {
            this@validateNumber
        } else {
            failure(errors)
        }
    }

@Suppress("unused")
fun <I, O> Maybe<I?>.validateUnsafe(block: I.(String) -> Maybe<O>): Maybe<O?> =
    onNotNull {
        @Suppress("UNCHECKED_CAST")
        value.block(this.context) as Maybe<O?>
    }

@Suppress("unused")
fun <I> Maybe<List<I?>?>.validateList(block: ListValidator.(List<I?>) -> Unit): Maybe<List<I?>?> =
    onNotNull {
        val errors = ListValidator(context).apply { this.block(value) }.validationErrors
        if (errors.isEmpty()) {
            this@validateList
        } else {
            failure(errors)
        }
    }

@Suppress("unused")
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
            failure(ValidationError(e.message ?: "is not a valid value", context))
        }
    }

/**
 * checks that the value of the maybe is not null
 * @return the given maybe or a [Maybe.Failure] if the value was null
 */
@Suppress("unused")
fun <T> Maybe<T?>.required(): Maybe<T> =
    onSuccess {
        if (value != null) {
            @Suppress("UNCHECKED_CAST")
            this as Maybe<T>
        } else {
            failure(ValidationError("is required", context))
        }
    }

/**
 * replaces a null value with the given value
 * @return the given maybe or a new [Maybe.Success] if the value was null
 */
@Suppress("unused")
fun <T> Maybe<T?>.default(block: () -> T): Maybe<T> =
    onSuccess {
        if (value == null) {
            success(block())
        } else {
            @Suppress("UNCHECKED_CAST")
            this as Maybe<T>
        }
    }

/**
 * executes the given block if this maybe has a value which is not null.
 */
@Suppress("unused")
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
