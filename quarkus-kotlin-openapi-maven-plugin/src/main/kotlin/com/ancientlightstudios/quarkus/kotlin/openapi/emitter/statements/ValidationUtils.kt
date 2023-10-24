package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.Expression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.validation.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.MethodName.Companion.methodName

fun CodeWriter.render(valueTransform: (String) -> Expression, validation: Validation) = when (validation) {
    is StringValidation -> render(validation)
    is NumberValidation -> render(valueTransform, validation)
    is ArrayValidation -> render(validation)
    is DefaultValidation -> render(validation)
}

private fun CodeWriter.render(validation: StringValidation) {
    if (validation.hasStringValidationRules) {
        writeln(".validateString {")
        indent {
            if (validation.minLength != null) {
                writeln("it.minLength(${validation.minLength})")
            }
            if (validation.maxLength != null) {
                writeln("it.maxLength(${validation.minLength})")
            }
            if (validation.pattern != null) {
                writeln("it.pattern(\"${validation.pattern}\")")
            }
        }
        writeln("}")
    }
    renderCustomConstraints(validation.customConstraints)
}

private fun CodeWriter.render(valueTransform: (String) -> Expression, validation: NumberValidation) {
    if (validation.hasNumberValidationRules) {
        writeln(".validateNumber {")
        indent {
            if (validation.minimum != null) {
                writeln("it.minimum(${valueTransform(validation.minimum.value).evaluate()}, ${validation.minimum.exclusive})")
            }
            if (validation.maximum != null) {
                writeln("it.minimum(${valueTransform(validation.maximum.value).evaluate()}, ${validation.maximum.exclusive})")
            }
        }
        writeln("}")
    }
    renderCustomConstraints(validation.customConstraints)
}

private fun CodeWriter.render(validation: ArrayValidation) {
    if (validation.hasArrayValidationRules) {
        writeln(".validateList {")
        indent {
            if (validation.minSize != null) {
                writeln("it.minSize(${validation.minSize})")
            }
            if (validation.maxSize != null) {
                writeln("it.maxSize(${validation.minSize})")
            }
        }
        writeln("}")
    }
    renderCustomConstraints(validation.customConstraints)
}

private fun CodeWriter.render(validation: DefaultValidation) = renderCustomConstraints(validation.customConstraints)

private fun CodeWriter.renderCustomConstraints(customConstraints: List<String>) {
    customConstraints.forEach {
        val methodName = "validate".methodName().extend(postfix = it)
        writeln(".validate(DefaultValidator::${methodName.render()})")
    }

}