package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization.DeserializationMethodEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.serialization.SerializationMethodEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*

class DefaultObjectModelClassEmitter(
    private val typeDefinition: ObjectTypeDefinition,
    private val needSerializeMethods: Boolean,
    private val needDeserializeMethods: Boolean
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

                if (needSerializeMethods) {
                    generateSerializeMethods()
                }

                if (needDeserializeMethods) {
                    kotlinCompanion {
                        generateDeserializeMethods()
                    }
                }

            }
        }.writeFile()
    }

    private fun KotlinClass.generateSerializeMethods() {
        typeDefinition.contentTypes.forEach {
            typeDefinition.contentTypes.forEach {
                addMethod(emitterContext.runEmitter(SerializationMethodEmitter(typeDefinition, it)).generatedMethod)
            }
        }
    }

    private fun KotlinCompanion.generateDeserializeMethods() {
        typeDefinition.contentTypes.forEach {
            addMethod(emitterContext.runEmitter(DeserializationMethodEmitter(typeDefinition, it)).generatedMethod)
        }
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