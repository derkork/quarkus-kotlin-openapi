package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class MaybePrimitiveTransformStatement(
    private val variableName: VariableName, private val parameterName: VariableName,
    private val contextName: String, private val type: TypeName
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        write("val ${variableName.name} = ${parameterName.name}.as")
        type.render(this)
        writeln("(\"$contextName\")")
    }
}