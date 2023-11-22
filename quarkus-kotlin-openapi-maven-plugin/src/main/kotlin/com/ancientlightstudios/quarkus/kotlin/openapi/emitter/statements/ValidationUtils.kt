package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.Expression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression.Companion.stringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.validation.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.MethodName.Companion.methodName

fun CodeWriter.render(valueTransform: (String) -> Expression, validations: List<Validation>) {
    renderStringValidation(validations)
    renderNumberValidation(valueTransform, validations)
    renderArrayValidation(validations)
    renderCustomConstraintsValidation(validations)
}

private fun CodeWriter.renderStringValidation(validations: List<Validation>) {
    val stringValidations = validations.filterIsInstance<StringValidation>()

    if (stringValidations.isNotEmpty()) {
        write(".validateString ")
        block(newLineAfter = true) {
            stringValidations.forEach {
                if (it.minLength != null) {
                    writeln("it.minLength(${it.minLength})")
                }
                if (it.maxLength != null) {
                    writeln("it.maxLength(${it.maxLength})")
                }
                if (it.pattern != null) {
                    writeln("it.pattern(${it.pattern.stringExpression().evaluate()})")
                }
            }
        }
    }
}

private fun CodeWriter.renderNumberValidation(valueTransform: (String) -> Expression, validations: List<Validation>) {
    val numberValidations = validations.filterIsInstance<NumberValidation>()

    if (numberValidations.isNotEmpty()) {
        write(".validateNumber ")
        block(newLineAfter = true) {
            numberValidations.forEach {
                if (it.minimum != null) {
                    writeln("it.minimum(${valueTransform(it.minimum.value).evaluate()}, ${it.minimum.exclusive})")
                }
                if (it.maximum != null) {
                    writeln("it.maximum(${valueTransform(it.maximum.value).evaluate()}, ${it.maximum.exclusive})")
                }
            }
        }
    }
}

private fun CodeWriter.renderArrayValidation(validations: List<Validation>) {
    val arrayValidations = validations.filterIsInstance<ArrayValidation>()

    if (arrayValidations.isNotEmpty()) {
        write(".validateList ")
        block(newLineAfter = true) {
            arrayValidations.forEach {
                if (it.minSize != null) {
                    writeln("it.minSize(${it.minSize})")
                }
                if (it.maxSize != null) {
                    writeln("it.maxSize(${it.maxSize})")
                }
            }
        }
    }
}

private fun CodeWriter.renderCustomConstraintsValidation(validations: List<Validation>) {
    val customConstraintsValidations = validations.filterIsInstance<CustomConstraintsValidation>()
        .flatMap { it.constraints }

    customConstraintsValidations.forEach {
        val methodName = "validate".methodName().extend(postfix = it)
        writeln(".validate(DefaultValidator::${methodName.render()})")
    }
}
