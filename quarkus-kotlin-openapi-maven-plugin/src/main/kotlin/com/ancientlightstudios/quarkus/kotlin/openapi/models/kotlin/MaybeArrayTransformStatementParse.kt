package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ValidationInfo
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.ifFalse
import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class MaybeArrayTransformStatementParse(
    private val variableName: VariableName, private val parameterName: Expression,
    private val context: Expression, private val type: TypeName, private val validationInfo: ValidationInfo
) : MaybeTransformStatement() {

    override fun render(writer: CodeWriter) = with(writer) {
        write("val ${variableName.name} = ${parameterName.expression}.asObject(")
        write(context.expression)
        write(", ")
        type.render(this)
        writeln("::class.java, objectMapper)")
        indent {
            renderValidation(validationInfo)
            writeln(".map { it${"?" ifFalse validationInfo.required}.toList() }") // todo: recursive lists
            writeln(".validated {")
            indent {
                writeln("it.validated()") //TODO: recursive lists && nullable items
            }
            writeln("}")
        }

    }
}