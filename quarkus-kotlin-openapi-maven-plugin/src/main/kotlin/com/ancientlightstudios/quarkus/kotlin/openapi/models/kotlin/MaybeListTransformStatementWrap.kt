package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ValidationInfo
import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class MaybeListTransformStatementWrap(
    private val variableName: VariableName, private val parameterName: Expression,
    private val context: Expression, private val validationInfo: ValidationInfo
) : MaybeTransformStatement() {

    override fun render(writer: CodeWriter) = with(writer) {
        writeln("val ${variableName.name} = ${parameterName.expression}.maybeOf(${context.expression})")

        indent {
            renderValidation(validationInfo)
            writeln(".validated {")
            indent {
                writeln("it.validated()") //TODO: recursive lists && nullable items
            }
            writeln("}")
        }
    }
}