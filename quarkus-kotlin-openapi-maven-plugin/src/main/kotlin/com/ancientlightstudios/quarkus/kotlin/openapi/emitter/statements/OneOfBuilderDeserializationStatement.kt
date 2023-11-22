package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.TypeDefinitionUsage

class OneOfBuilderDeserializationStatement(val objectName: ClassName) : KotlinStatement {

    private val parameters = mutableListOf<Pair<VariableName,TypeDefinitionUsage>>()

    fun addParameter(maybeName: VariableName, innerTypeDefinition:TypeDefinitionUsage) {
        parameters.add(maybeName to innerTypeDefinition)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        write("return maybeOneOf(context")
        parameters.forEach { (variableName, _) ->
            write(", ${variableName.render()}")
        }
        write(") ")
        block(newLineAfter = true) {
            parameters.forEach { (variableName, innerTypeDefinition) ->
                writeln("${variableName.render()}.validValueOrNull()?.let { return@maybeOneOf ${objectName.extend(postfix = innerTypeDefinition.safeType.className().render()).render()}(it) }")
            }
            writeln("throw IllegalStateException(\"at least one option should be available\")")
        }
    }

}
