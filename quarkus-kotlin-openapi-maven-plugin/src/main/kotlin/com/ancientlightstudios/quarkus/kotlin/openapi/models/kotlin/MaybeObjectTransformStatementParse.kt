package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ValidationInfo
import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class MaybeObjectTransformStatementParse(
    private val variableName: VariableName, private val parameterName: Expression,
    private val context: Expression, private val type: TypeName, private val validationInfo: ValidationInfo
) : MaybeTransformStatement() {

    override fun render(writer: CodeWriter) = with(writer) {
        write("val ${variableName.name} = ${parameterName.expression}.asObject(${context.expression}, ")
        type.render(this)
        writeln("::class.java, objectMapper)")

        indent {
            writeln(".validated()")
            renderValidation(validationInfo)
        }
    }
}