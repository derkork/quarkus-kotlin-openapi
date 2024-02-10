package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.InterfaceType
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect

class AnyOfObjectModelClassEmitter(private val interfaceType: InterfaceType) : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.inspect {
            schemaDefinitions {
                // (schemaDefinition.typeDefinition as? AnyOfTypeDefinition)?.apply { emitModelFile().writeFile() }
            }
        }
    }

//    private fun AnyOfTypeDefinition.emitModelFile() = kotlinFile(className) {
//        // TODO: data class
//        kotlinClass(fileName) {
//        }
//    }

}