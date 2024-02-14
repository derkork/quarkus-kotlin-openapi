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

                val serializationContentTypes = when (isServer) {
                    // server serialize the response
                    true -> typeDefinition.getContentTypes(Direction.Down)
                    // clients serialize the request
                    else -> typeDefinition.getContentTypes(Direction.Up)
                }

                val deserializationContentTypes = when (isServer) {
                    // server deserialize the request
                    true -> typeDefinition.getContentTypes(Direction.Up)
                    // clients deserialize the response
                    else -> typeDefinition.getContentTypes(Direction.Down)
                }

                when (typeDefinition) {
                    is CollectionTypeDefinition,
                    is PrimitiveTypeDefinition -> {
                        // build-in, so nothing to do here
                    }

                    is EnumTypeDefinition -> runEmitter(
                        EnumModelClassEmitter(typeDefinition, serializationContentTypes, deserializationContentTypes)
                    )

                    is ObjectTypeDefinition -> runEmitter(
                        DefaultObjectModelClassEmitter(
                            typeDefinition, serializationContentTypes, deserializationContentTypes
                        )
                    )
                }
            }
        }

    }

}
