package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ValidationInfo
import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class MaybeObjectTransformStatementWrap(
    private val variableName: VariableName, private val parameterName: Expression,
    private val context: Expression, private val validationInfo: ValidationInfo
) : MaybeTransformStatement() {

    override fun render(writer: CodeWriter) = with(writer) {
        writeln("val ${variableName.name} = ${parameterName.expression}.asMaybe(${context.expression})")

        indent {
            renderValidation(validationInfo)
            writeln(".validated()")
        }
    }
}