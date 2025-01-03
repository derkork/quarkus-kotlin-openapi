package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*

//class ModelClassEmitter(private val withTestSupport: Boolean) : CodeEmitter {
//
//    override fun EmitterContext.emit() {
//        spec.modelTypes.forEach { typeDefinition ->
//            when (typeDefinition) {
//                is CollectionTypeDefinition,
//                is PrimitiveTypeDefinition -> {
//                    // build-in, so nothing to do here
//                }
//
//                is EnumTypeDefinition -> runEmitter(EnumModelClassEmitter(typeDefinition))
//                is ObjectTypeDefinition -> {
//                    if (!typeDefinition.isPureMap) {
//                        runEmitter(ObjectModelClassEmitter(typeDefinition, withTestSupport))
//                    }
//                    // pure maps are build-in, so nothing to do here
//                }
//                is OneOfTypeDefinition -> runEmitter(OneOfModelClassEmitter(typeDefinition, withTestSupport))
//            }
//        }
//    }
//
//}
