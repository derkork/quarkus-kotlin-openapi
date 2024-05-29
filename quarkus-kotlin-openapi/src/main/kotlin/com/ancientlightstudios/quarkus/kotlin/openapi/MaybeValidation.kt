package com.ancientlightstudios.quarkus.kotlin.openapi

@Suppress("unused")
fun <T> Maybe<T?>.validate(block: DefaultValidator.(T) -> Unit): Maybe<T?> =
    onNotNull {
        try {
            val errors = DefaultValidator(context).apply { this.block(value) }.validationErrors
            if (errors.isEmpty()) {
                this@validate
            } else {
                failure(errors)
            }
        } catch (_: Exception) {
            failure(ValidationError("is not a valid value", context))
        }
    }

@Suppress("unused")
fun Maybe<String?>.validateString(block: StringValidator.(String) -> Unit): Maybe<String?> =
    onNotNull {
        try {
            val errors = StringValidator(context).apply { this.block(value) }.validationErrors
            if (errors.isEmpty()) {
                this@validateString
            } else {
                failure(errors)
            }
        } catch (_: Exception) {
            failure(ValidationError("is not a valid value", context))
        }
    }

@Suppress("unused")
@JvmName("validateNumber")
fun <T : Number> Maybe<T?>.validateNumber(block: NumberValidator<T>.(T) -> Unit): Maybe<T?> =
    onNotNull {
        try {
            val errors = NumberValidator<T>(context).apply { this.block(value) }.validationErrors
            if (errors.isEmpty()) {
                this@validateNumber
            } else {
                failure(errors)
            }
        } catch (_: Exception) {
            failure(ValidationError("is not a valid value", context))
        }
    }

@Suppress("unused")
@JvmName("validateULongNumber")
fun Maybe<ULong?>.validateNumber(block: NumberValidator<ULong>.(ULong) -> Unit): Maybe<ULong?> =
    onNotNull {
        try {
            val errors = NumberValidator<ULong>(context).apply { this.block(value) }.validationErrors
            if (errors.isEmpty()) {
                this@validateNumber
            } else {
                failure(errors)
            }
        } catch (_: Exception) {
            failure(ValidationError("is not a valid value", context))
        }
    }

@Suppress("unused")
@JvmName("validateUIntNumber")
fun Maybe<UInt?>.validateNumber(block: NumberValidator<UInt>.(UInt) -> Unit): Maybe<UInt?> =
    onNotNull {
        try {
            val errors = NumberValidator<UInt>(context).apply { this.block(value) }.validationErrors
            if (errors.isEmpty()) {
                this@validateNumber
            } else {
                failure(errors)
            }
        } catch (_: Exception) {
            failure(ValidationError("is not a valid value", context))
        }
    }

@Suppress("unused")
@JvmName("validateUShortNumber")
fun Maybe<UShort?>.validateNumber(block: NumberValidator<UShort>.(UShort) -> Unit): Maybe<UShort?> =
    onNotNull {
        try {
            val errors = NumberValidator<UShort>(context).apply { this.block(value) }.validationErrors
            if (errors.isEmpty()) {
                this@validateNumber
            } else {
                failure(errors)
            }
        } catch (_: Exception) {
            failure(ValidationError("is not a valid value", context))
        }
    }

@Suppress("unused")
@JvmName("validateUByteNumber")
fun Maybe<UByte?>.validateNumber(block: NumberValidator<UByte>.(UByte) -> Unit): Maybe<UByte?> =
    onNotNull {
        try {
            val errors = NumberValidator<UByte>(context).apply { this.block(value) }.validationErrors
            if (errors.isEmpty()) {
                this@validateNumber
            } else {
                failure(errors)
            }
        } catch (_: Exception) {
            failure(ValidationError("is not a valid value", context))
        }
    }

@Suppress("unused")
fun <I> Maybe<List<I>?>.validateList(block: ListValidator.(List<I>) -> Unit): Maybe<List<I>?> =
    onNotNull {
        try {
            val errors = ListValidator(context).apply { this.block(value) }.validationErrors
            if (errors.isEmpty()) {
                this@validateList
            } else {
                failure(errors)
            }
        } catch (_: Exception) {
            failure(ValidationError("is not a valid value", context))
        }
    }

@Suppress("unused")
fun <I, O> Maybe<List<I?>?>.mapItems(block: (Maybe.Success<I?>) -> Maybe<O>): Maybe<List<O>?> =
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
        } catch (_: Exception) {
            failure(ValidationError("is not a valid value", context))
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
fun <T> Maybe<T?>.default(block: () -> T): Maybe<T?> =
    onSuccess {
        if (value == null) {
            success(block())
        } else {
            this
        }
    }

/**
 * executes the given block if this maybe has a value which is not null.
 */
@Suppress("unused")
inline fun <I, O> Maybe<out I?>.onNotNull(crossinline block: Maybe.Success<I & Any>.() -> Maybe<O?>): Maybe<O?> =
    onSuccess {
        if (value != null) {
            @Suppress("UNCHECKED_CAST")
            (this as Maybe.Success<I & Any>).block()
        } else {
            @Suppress("UNCHECKED_CAST")
            this as Maybe<O?>
        }
    }
