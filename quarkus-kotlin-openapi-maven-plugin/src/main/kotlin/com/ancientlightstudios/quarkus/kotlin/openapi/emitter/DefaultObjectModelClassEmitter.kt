package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization.DeserializationMethodEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.serialization.SerializationMethodEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*

class DefaultObjectModelClassEmitter(
    private val typeDefinition: ObjectTypeDefinition,
    private val serializeContentTypes: Set<ContentType>,
    private val deserializeContentTypes: Set<ContentType>
) : CodeEmitter {

    private lateinit var emitterContext: EmitterContext

    override fun EmitterContext.emit() {
        emitterContext = this

        kotlinFile(typeDefinition.modelName) {
            registerImports(Library.AllClasses)
            registerImports(getAdditionalImports())

            kotlinClass(fileName, asDataClass = true) {
                typeDefinition.properties.forEach {
                    val forceNullable = !typeDefinition.required.contains(it.sourceName)
                    val defaultValue = generateDefaultValueExpression(it.schema.typeDefinition, forceNullable)
                    kotlinMember(
                        it.name,
                        it.schema.typeDefinition.buildValidType(forceNullable),
                        accessModifier = null,
                        default = defaultValue
                    )
                }

                // for objects there is only json in both directions. everything else will be managed by the rest interface
                if (serializeContentTypes.contains(ContentType.ApplicationJson)) {
                    addMethod(
                        emitterContext.runEmitter(
                            SerializationMethodEmitter(typeDefinition, ContentType.ApplicationJson)
                        ).generatedMethod
                    )
                }

                if (deserializeContentTypes.contains(ContentType.ApplicationJson)) {
                    kotlinCompanion {
                        addMethod(
                            emitterContext.runEmitter(
                                DeserializationMethodEmitter(typeDefinition, ContentType.ApplicationJson)
                            ).generatedMethod
                        )
                    }
                }

            }
        }.writeFile()
    }

    private fun generateDefaultValueExpression(
        propertyType: TypeDefinition, forceNullable: Boolean
    ): KotlinExpression? {
        val declaredDefaultValue = when (propertyType) {
            is PrimitiveTypeDefinition -> propertyType.defaultValue
            is EnumTypeDefinition -> propertyType.defaultValue
            is CollectionTypeDefinition,
            is ObjectTypeDefinition -> null
        }

        val canBeNull = forceNullable || propertyType.nullable
        // if there is a default expression defined, use it. Otherwise, use the null expression, if null is allowed
        return declaredDefaultValue ?: if (canBeNull) nullLiteral() else null
    }

}