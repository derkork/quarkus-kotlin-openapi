package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements.writeSerializationStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.ClassExpression.Companion.classExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.ExtendFromInterfaceExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.InvocationExpression.Companion.invocationExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.NullExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.PathExpression.Companion.pathExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression.Companion.stringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.OneOfTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.TypeDefinitionRegistry
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.forEachWithStats

class SafeOneOfModelEmitter : CodeEmitter {

    override fun EmitterContext.emit(suite: RequestSuite, typeDefinitionRegistry: TypeDefinitionRegistry) {
        typeDefinitionRegistry.getAllTypeDefinitions()
            .filterIsInstance<OneOfTypeDefinition>()
            .forEach {
                emitModel(it)
            }
    }

    private fun EmitterContext.emitModel(definition: OneOfTypeDefinition) {
        kotlinFile(modelPackage(), definition.name) {
            registerImport(apiPackage(), wildcardImport = true)
            registerImport("com.fasterxml.jackson.databind.JsonNode")
            registerImport("com.fasterxml.jackson.databind.node.NullNode")


            kotlinInterface(fileName, sealed = true) {
                kotlinMethod("toJsonNode".methodName(), returnType = "JsonNode".rawTypeName())
            }

            definition.schemas.forEach { (type, values) ->
                kotlinClass(fileName.extend(postfix = type.safeType.className().render()), asDataClass = true,
                    extends = listOf(ExtendFromInterfaceExpression(fileName.className()))) {
                    kotlinMember("value".variableName(), type.safeType, accessModifier = null)
                    // toJsonNode
                    kotlinMethod(
                        "toJsonNode".methodName(),
                        returnType = "JsonNode".rawTypeName(),
                        bodyAsAssignment = true,
                        override = true
                    ) {
                        kotlinStatement {
                            val expression = if (definition.discriminator != null) {
                                "value".variableName().pathExpression(type.nullable).then("copy".methodName())
                                    .invocationExpression(
                                        definition.discriminator.variableName() to values.first()
                                            .stringExpression()
                                    )
                            } else {
                                "value".variableName().pathExpression()
                            }
                            writeSerializationStatement(expression, type)
                            if (type.nullable) {
                                write(" ?: NullNode.instance")
                            }
                        }
                    }
                }

            }
        }.also { generateFile(it) }
    }
}