package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.InterfaceType
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinClass
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinFile
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinMember
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.ObjectTypeDefinition

class DefaultObjectModelClassEmitter(private val interfaceType: InterfaceType) : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.inspect {
            schemaDefinitions {
                (schemaDefinition.typeDefinition as? ObjectTypeDefinition)?.apply { emitModelFile().writeFile() }
            }
        }
    }

    private fun ObjectTypeDefinition.emitModelFile() = kotlinFile(className) {
        kotlinClass(fileName, asDataClass = true) {
            properties.forEach {
                kotlinMember(
                    it.name.variableName(),
                    it.schema.buildValidType(),
                    accessModifier = null,
                    // default = propertyTypeUsage.defaultValue // TODO: default value
                )
            }

        }
    }

}