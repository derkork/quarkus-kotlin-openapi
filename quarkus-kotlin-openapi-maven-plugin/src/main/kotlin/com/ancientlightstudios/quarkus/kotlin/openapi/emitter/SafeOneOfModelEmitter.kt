package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.NullExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.PathExpression.Companion.pathExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.rawClassName
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

                definition.schemas.forEach { type ->
                    kotlinMember(
                        type.safeType.variableName(),
                        type.safeType,
                        accessModifier = null
                    ) {
                        kotlinAnnotation("field:JsonUnwrapped".rawClassName())
                    }
                }

                if (definition.schemas.size > 1) {
                    definition.schemas.forEachIndexed { index, item ->
                        kotlinConstructor {
                            kotlinParameter(item.safeType.variableName(), item.safeType.alterNullable(false))
                            definition.schemas.forEachIndexed { innerIndex, _ ->
                                if (index == innerIndex) {
                                    addPrimaryConstructorParameter(item.safeType.variableName().pathExpression())
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