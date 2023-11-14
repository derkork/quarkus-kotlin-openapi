package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements.writeToJsonNode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.InvocationExpression.Companion.invocationExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.NullExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.PathExpression.Companion.pathExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression.Companion.stringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.typeName
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

            val primaryConstructorAccessModifier = when (definition.schemas.size) {
                1 -> null
                else -> KotlinAccessModifier.Private
            }

            kotlinClass(fileName, asDataClass = true, constructorAccessModifier = primaryConstructorAccessModifier) {

                definition.schemas.forEach { (type, _) ->
                    kotlinMember(type.safeType.variableName(), type.safeType, accessModifier = null)
                }

                if (definition.schemas.size > 1) {
                    definition.schemas.keys.forEachIndexed { index, item ->
                        kotlinConstructor {
                            kotlinParameter(item.safeType.variableName(), item.safeType.alterNullable(false))
                            definition.schemas.entries.forEachIndexed { innerIndex, (_, value) ->
                                if (index == innerIndex) {
                                    val constructorParameter = if (definition.discriminator != null) {
                                        item.safeType.variableName().pathExpression().then("copy".methodName())
                                            .invocationExpression(
                                                definition.discriminator.variableName() to value.first()
                                                    .stringExpression()
                                            )
                                    } else {
                                        item.safeType.variableName().pathExpression()
                                    }
                                    addPrimaryConstructorParameter(constructorParameter)
                                } else {
                                    addPrimaryConstructorParameter(NullExpression)
                                }
                            }
                        }
                    }
                }

                // toJsonNode
                kotlinMethod("toJsonNode".methodName(), returnType = "JsonNode".rawTypeName(), bodyAsAssignment = true) {
                    kotlinStatement {
                        definition.schemas.keys.forEachWithStats { status, typeDefinitionUsage ->
                            if (status.first) {
                                writeToJsonNode(typeDefinitionUsage.safeType.variableName(), typeDefinitionUsage)
                            }
                            else {
                                indent {
                                    write(".shallowMerge(")
                                    writeToJsonNode(typeDefinitionUsage.safeType.variableName(), typeDefinitionUsage)
                                    write(")")
                                }
                            }
                            if (status.last) {
                                write("!!")
                            }
                            else {
                                writeln()
                            }
                        }
                    }
                }

            }
        }.also { generateFile(it) }
    }
}