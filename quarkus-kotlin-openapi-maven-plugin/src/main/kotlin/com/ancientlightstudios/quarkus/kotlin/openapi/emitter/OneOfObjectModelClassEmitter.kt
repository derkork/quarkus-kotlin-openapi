package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.InterfaceType
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinClass
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinFile
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.OneOfTypeDefinition

class OneOfObjectModelClassEmitter(private val interfaceType: InterfaceType) : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.inspect {
            schemaDefinitions {
                (schemaDefinition.typeDefinition as? OneOfTypeDefinition)?.apply { emitModelFile().writeFile() }
            }
        }
    }

    private fun OneOfTypeDefinition.emitModelFile() = kotlinFile(className) {
        // TODO: data class
        kotlinClass(fileName) {
        }
    }

}