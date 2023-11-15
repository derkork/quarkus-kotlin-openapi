package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements.AnyOfBuilderDeserializationStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements.getDeserializationStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.PathExpression.Companion.pathExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.AnyOfTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.FlowDirection
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.TypeDefinitionRegistry

class UnsafeAnyOfModelEmitter(private val direction: FlowDirection) : CodeEmitter {

    override fun EmitterContext.emit(suite: RequestSuite, typeDefinitionRegistry: TypeDefinitionRegistry) {
        typeDefinitionRegistry.getAllTypeDefinitions(direction)
            .filterIsInstance<AnyOfTypeDefinition>()
            .forEach {
                emitModel(it)
            }
    }

    private fun EmitterContext.emitModel(definition: AnyOfTypeDefinition) {
        kotlinFile(modelPackage(), definition.name.extend(postfix = "Unsafe")) {
            registerImport("com.fasterxml.jackson.databind.JsonNode")
            registerImport(apiPackage(), wildcardImport = true)
            registerImports(additionalImports)

            kotlinClass(fileName, asDataClass = true) {
                kotlinMember("node".variableName(), "JsonNode".rawTypeName())

                kotlinMethod("asSafe".methodName(), returnType = "Maybe".typeName().of(definition.name)) {
                    kotlinParameter("context".variableName(), "String".rawTypeName())

                    val returnStatement = AnyOfBuilderDeserializationStatement(definition.name)

                    definition.schemas.forEach { schema ->
                        val parameter = schema.safeType.variableName().extend(postfix = "maybe")
                        val source = "node".variableName().parameterToMaybeExpression(
                            "context".variableName().pathExpression()
                        )
                        addStatement(getDeserializationStatement(source, parameter, schema, true))
                        returnStatement.addParameter(parameter)
                    }

                    addStatement(returnStatement)
                }
            }
        }.also { generateFile(it) }
    }
}