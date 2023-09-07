package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ValidationInfo
import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class MaybeObjectTransformStatement(
    private val variableName: VariableName, private val parameterName: VariableName,
    private val contextName: String, private val type: TypeName, private val validationInfo: ValidationInfo
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        write("val ${variableName.name} = ${parameterName.name}.asObject(\"$contextName\", ")
        type.render(this)
        writeln("::class.java, objectMapper)")

        if (validationInfo.required) {
            indent {
                writeln(".required()")
            }
        }
    }
}