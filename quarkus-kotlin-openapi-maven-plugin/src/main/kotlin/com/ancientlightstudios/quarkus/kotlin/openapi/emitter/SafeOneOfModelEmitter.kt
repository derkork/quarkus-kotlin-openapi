package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.InvocationExpression.Companion.invocationExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.NullExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.PathExpression.Companion.pathExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression.Companion.stringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.OneOfTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.TypeDefinitionRegistry

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
            registerImport("io.quarkus.runtime.annotations.RegisterForReflection")
            registerImport("com.fasterxml.jackson.annotation.JsonUnwrapped")
            registerImport(apiPackage(), wildcardImport = true)

            val primaryConstructorAccessModifier = when (definition.schemas.size) {
                1 -> null
                else -> KotlinAccessModifier.Private
            }

            kotlinClass(fileName, asDataClass = true, constructorAccessModifier = primaryConstructorAccessModifier) {
                addReflectionAnnotation()

                definition.schemas.forEach { (type,_ ) ->
                    kotlinMember(
                        type.safeType.variableName(),
                        type.safeType,
                        accessModifier = null
                    ) {
                        kotlinAnnotation("field:JsonUnwrapped".rawClassName())
                    }
                }

                if (definition.schemas.size > 1) {
                    definition.schemas.keys.forEachIndexed { index, item ->
                        kotlinConstructor {
                            kotlinParameter(item.safeType.variableName(), item.safeType.alterNullable(false))
                            definition.schemas.entries.forEachIndexed { innerIndex, (_, value) ->
                                if (index == innerIndex) {
                                    val constructorParameter = if (definition.discriminator != null) {
                                        item.safeType.variableName().pathExpression().then("copy".methodName())
                                            .invocationExpression(definition.discriminator.variableName() to value.first().stringExpression() )
                                    }
                                    else {
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

            }
        }.also { generateFile(it) }
    }
}