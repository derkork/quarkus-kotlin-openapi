package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression.Companion.stringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinAnnotation
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinClass
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinFile
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinMember
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.ObjectTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.TypeDefinitionRegistry
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.overrideWhenOptional

class SafeModelEmitter : CodeEmitter {

    override fun EmitterContext.emit(suite: RequestSuite, typeDefinitionRegistry: TypeDefinitionRegistry) {
        typeDefinitionRegistry.getAllTypeDefinitions()
            .filterIsInstance<ObjectTypeDefinition>()
            .forEach {
                emitModel(it)
            }
    }

    private fun EmitterContext.emitModel(definition: ObjectTypeDefinition) {
        kotlinFile(modelPackage(), definition.name) {
            registerImport("io.quarkus.runtime.annotations.RegisterForReflection")
            registerImport("com.fasterxml.jackson.annotation.JsonProperty")
            registerImport(apiPackage(), wildcardImport = true)

            kotlinClass(fileName, asDataClass = true) {
                addReflectionAnnotation()

                definition.sourceSchema.properties
                    .filter { (_, property) -> definition.propertyFilter(property) }
                    .forEach { (name, property) ->
                        val propertyTypeDefinition = definition.resolveSchema(property)
                        kotlinMember(
                            name.variableName(),
                            propertyTypeDefinition.defaultType.overrideWhenOptional(!property.required),
                            private = false
                        ) {
                            kotlinAnnotation(
                                "field:JsonProperty".rawClassName(),
                                "value".variableName() to name.stringExpression()
                            )
                        }
                    }
            }
        }.also { generateFile(it) }
    }
}