package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization.DeserializationMethodEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.serialization.SerializationMethodEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.EnumTypeDefinition

class EnumModelClassEmitter(
    private val typeDefinition: EnumTypeDefinition,
    private val needSerializeMethods: Boolean,
    private val needDeserializeMethods: Boolean
) : CodeEmitter {

    private lateinit var emitterContext: EmitterContext

    override fun EmitterContext.emit() {
        emitterContext = this

        kotlinFile(typeDefinition.modelName) {
            registerImports(Library.AllClasses)
            registerImports(getAdditionalImports())

            kotlinEnum(fileName) {
                kotlinMember(
                    "value".variableName(),
                    typeDefinition.baseType.typeName(),
                    accessModifier = null
                )
                typeDefinition.items.forEach {
                    kotlinEnumItem(it.name, it.value)
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

    private fun KotlinEnum.generateSerializeMethods() {
        typeDefinition.contentTypes.forEach {
            addMethod(emitterContext.runEmitter(SerializationMethodEmitter(typeDefinition, it)).generatedMethod)
        }
    }

    private fun KotlinCompanion.generateDeserializeMethods() {
        // deserialization for enum reuse the plain method, so we always have to generate it
        addMethod(
            emitterContext.runEmitter(
                DeserializationMethodEmitter(typeDefinition, ContentType.TextPlain)
            ).generatedMethod
        )

        typeDefinition.contentTypes.minus(ContentType.TextPlain).forEach {
            addMethod(emitterContext.runEmitter(DeserializationMethodEmitter(typeDefinition, it)).generatedMethod)
        }
    }

}