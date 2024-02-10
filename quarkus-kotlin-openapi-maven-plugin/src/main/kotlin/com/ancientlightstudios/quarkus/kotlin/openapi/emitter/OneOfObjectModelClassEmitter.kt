package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.InterfaceType
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect

class OneOfObjectModelClassEmitter(private val interfaceType: InterfaceType) : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.inspect {
            schemaDefinitions {
//                (schemaDefinition.typeDefinition as? OneOfTypeDefinition)?.apply { emitModelFile().writeFile() }
            }
        }
    }

//    private fun OneOfTypeDefinition.emitModelFile() = kotlinFile(className) {
//        // TODO: data class
//        kotlinClass(fileName) {
//        }
//    }

}