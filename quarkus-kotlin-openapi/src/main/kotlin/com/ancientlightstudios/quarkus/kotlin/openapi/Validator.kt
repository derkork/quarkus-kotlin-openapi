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
class NumberValidator<T>(context: String) : Validator(context) {

    fun T.minimum(min: Comparable<T>, exclusive: Boolean) {
        if (exclusive) {
            if (min >= this) {
                reportError("exclusive minimum of $min expected, but is $this")
            }
        } else {
            if (min > this) {
                reportError(" minimum of $min expected, but is $this")
            }
        }
    }

    fun T.maximum(max: Comparable<T>, exclusive: Boolean) {
        if (exclusive) {
            if (max <= this) {
                reportError("exclusive maximum of $max expected, but is $this")
            }
        } else {
            if (max < this) {
                reportError(" maximum of $max expected, but is $this")
            }
        }
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
        if (size > max) {
            reportError("maximum size of $max expected, but is $size")
        }
    }
}