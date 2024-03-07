package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.CollectionTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.EnumTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.ObjectTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.PrimitiveTypeDefinition

class ModelClassEmitter : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.inspect {
            // TODO: apply filter to ignore types which were generated, but are not directly used anywhere
            schemaDefinitions {
                when (val typeDefinition = schemaDefinition.typeDefinition) {
                    is CollectionTypeDefinition,
                    is PrimitiveTypeDefinition -> {
                        // build-in, so nothing to do here
                    }

                    is EnumTypeDefinition -> runEmitter(EnumModelClassEmitter(typeDefinition))
                    is ObjectTypeDefinition -> runEmitter(DefaultObjectModelClassEmitter(typeDefinition))
                }
            }
        }

    }

}
