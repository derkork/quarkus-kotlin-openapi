package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.InterfaceType
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinClass
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinComment
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

    private fun ObjectTypeDefinition.emitModelFile() = kotlinFile(modelName) {
        kotlinClass(fileName, asDataClass = true) {
            properties.forEach {
                kotlinMember(
                    it.name,
                    it.schema.typeDefinition.buildValidType(!required.contains(it.sourceName)),
                    accessModifier = null,
                    // default = propertyTypeUsage.defaultValue // TODO: default value or null if nullable
                )
            }

            kotlinComment {
                addLine(directions.joinToString { it.name })
                addLine(contentTypes.joinToString { it.name })
            }

        }
    }

}