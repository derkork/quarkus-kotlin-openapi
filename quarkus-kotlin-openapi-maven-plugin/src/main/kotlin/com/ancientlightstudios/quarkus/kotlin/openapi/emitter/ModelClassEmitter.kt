package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.InterfaceType
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*

class ModelClassEmitter(private val interfaceType: InterfaceType) : CodeEmitter {

    override fun EmitterContext.emit() {
        val isServer = interfaceType == InterfaceType.SERVER
        val isClient = interfaceType == InterfaceType.CLIENT

        spec.inspect {
            // TODO: apply filter to ignore types which were generated, but are not directly used anywhere
            schemaDefinitions {
                val typeDefinition = schemaDefinition.typeDefinition
                val hasUpDirection = typeDefinition.directions.contains(Direction.Up)
                val hasDownDirection = typeDefinition.directions.contains(Direction.Down)

                val needSerialize = (isClient && hasUpDirection) || (isServer && hasDownDirection)
                val needDeserialize = (isClient && hasDownDirection) || (isServer && hasUpDirection)

                when (typeDefinition) {
                    is CollectionTypeDefinition,
                    is PrimitiveTypeDefinition -> {
                        // build-in, so nothing to do here
                    }

                    is EnumTypeDefinition -> runEmitter(
                        EnumModelClassEmitter(typeDefinition, needSerialize, needDeserialize)
                    )

                    is ObjectTypeDefinition -> runEmitter(
                        DefaultObjectModelClassEmitter(typeDefinition, needSerialize, needDeserialize)
                    )
                }
            }
        }

    }

}
