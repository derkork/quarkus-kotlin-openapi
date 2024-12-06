package com.ancientlightstudios.quarkus.kotlin.openapi

open class Validator(private val context: String) {

    private val _validationErrors = mutableListOf<ValidationError>()

    val validationErrors: List<ValidationError>
        get() = _validationErrors

    protected fun reportError(reason: String, path: String, kind: ErrorKind) {
        _validationErrors.add(ValidationError(reason, "${context}.$path", kind))
    }

    protected fun reportError(reason: String, kind: ErrorKind) {
        _validationErrors.add(ValidationError(reason, context, kind))
    }

}

@Suppress("unused")
class DefaultValidator(context: String) : Validator(context) {

    fun fail(reason: String, kind: ErrorKind) = reportError(reason, kind)

    fun fail(reason: String, path: String, kind: ErrorKind) = reportError(reason, path, kind)

}

@Suppress("unused")
class StringValidator(context: String) : Validator(context) {

    fun String.minLength(min: Int) {
        if (length < min) {
            reportError("minimum length of $min expected, but is $length", ErrorKind.Invalid)
        }
    }

    fun String.maxLength(max: Int) {
        if (length > max) {
            reportError("maximum length of $max expected, but is $length", ErrorKind.Invalid)
        }
    }

    fun String.pattern(pattern: String) {
        val regex = PatternCache.compilePattern(pattern)
        if (!regex.matches(this)) {
            reportError("doesn't match pattern '$pattern'", ErrorKind.Invalid)
        }
    }

}

@Suppress("unused")
class ByteArrayValidator(context: String) : Validator(context) {

    fun ByteArray.minLength(min: Int) {
        if (size < min) {
            reportError("minimum length of $min expected, but is $size", ErrorKind.Invalid)
        }
    }

    fun ByteArray.maxLength(max: Int) {
        if (size > max) {
            reportError("maximum length of $max expected, but is $size", ErrorKind.Invalid)
        }
    }

}

@Suppress("unused")
class NumberValidator<T>(context: String) : Validator(context) {

    fun T.minimum(min: Comparable<T>, exclusive: Boolean) {
        if (exclusive) {
            if (min >= this) {
                reportError("exclusive minimum of $min expected, but is $this", ErrorKind.Invalid)
            }
        } else {
            if (min > this) {
                reportError(" minimum of $min expected, but is $this", ErrorKind.Invalid)
            }
        }
    }

    fun T.maximum(max: Comparable<T>, exclusive: Boolean) {
        if (exclusive) {
            if (max <= this) {
                reportError("exclusive maximum of $max expected, but is $this", ErrorKind.Invalid)
            }
        } else {
            if (max < this) {
                reportError(" maximum of $max expected, but is $this", ErrorKind.Invalid)
            }
        }
    }

}

@Suppress("unused")
class ListValidator(context: String) : Validator(context) {

    fun List<*>.minSize(min: Int) {
        if (size < min) {
            reportError("minimum size of $min expected, but is $size", ErrorKind.Invalid)
        }
    }

    fun List<*>.maxSize(max: Int) {
        if (size > max) {
            reportError("maximum size of $max expected, but is $size", ErrorKind.Invalid)
        }
    }
}

@Suppress("unused")
class PropertiesValidator(context: String): Validator(context) {

    fun Map<String, *>.minProperties(min: Int) {
        if (size < min) {
            reportError("minimum size of $min expected, but is $size", ErrorKind.Invalid)
        }
    }

    fun Map<String, *>.maxProperties(max: Int) {
        if (size > max) {
            reportError("maximum size of $max expected, but is $size", ErrorKind.Invalid)
        }
    }

    fun PropertiesContainer.minProperties(min: Int) {
        val size = receivedPropertiesCount()
        if (size < min) {
            reportError("minimum size of $min expected, but is $size", ErrorKind.Invalid)
        }
    }

    fun PropertiesContainer.maxProperties(max: Int) {
        val size = receivedPropertiesCount()
        if (size > max) {
            reportError("maximum size of $max expected, but is $size", ErrorKind.Invalid)
        }
    }

}

interface PropertiesContainer {

    fun receivedPropertiesCount(): Int

}