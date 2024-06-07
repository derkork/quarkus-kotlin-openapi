package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ModelTypesHint.modelTypes
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*

class ModelClassEmitter(private val withTestSupport: Boolean) : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.modelTypes.forEach { typeDefinition ->
            when (typeDefinition) {
                is CollectionTypeDefinition,
                is PrimitiveTypeDefinition -> {
                    // build-in, so nothing to do here
                }

                is EnumTypeDefinition -> runEmitter(EnumModelClassEmitter(typeDefinition))
                is ObjectTypeDefinition -> runEmitter(ObjectModelClassEmitter(typeDefinition, withTestSupport))
                is OneOfTypeDefinition -> runEmitter(OneOfModelClassEmitter(typeDefinition, withTestSupport))
            }
        }
    }

}
