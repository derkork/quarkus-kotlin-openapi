package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName

class OneOfBuilderTransformStatement(val objectName: ClassName) : KotlinStatement {

    private val parameters = mutableListOf<VariableName>()

    fun addParameter(maybeName: VariableName) {
        parameters.add(maybeName)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        write("return maybeOneOf(context")
        parameters.forEach { maybeName ->
            write(", ${maybeName.render()}")
        }
        write(") ")
        block(newLineAfter = true) {
            parameters.forEach {
                writeln("${it.render()}.validValueOrNull()?.let { return@maybeOneOf ${objectName.render()}(it) }")
            }
            writeln("throw IllegalStateException(\"at least one option should be available\")")
        }
    }

}
