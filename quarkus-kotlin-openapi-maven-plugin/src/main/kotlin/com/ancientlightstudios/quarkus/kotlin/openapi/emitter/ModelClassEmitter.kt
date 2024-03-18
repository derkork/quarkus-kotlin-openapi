package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ModelTypesHint.modelTypes
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.CollectionTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.EnumTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.ObjectTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.PrimitiveTypeDefinition

class ModelClassEmitter : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.modelTypes.forEach { typeDefinition ->
            when (typeDefinition) {
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
