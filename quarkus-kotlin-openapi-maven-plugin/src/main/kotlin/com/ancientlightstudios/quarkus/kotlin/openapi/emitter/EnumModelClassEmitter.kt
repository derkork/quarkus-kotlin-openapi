package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.InterfaceType
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinEnum
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinEnumItem
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinFile
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinMember
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.EnumTypeDefinition

class EnumModelClassEmitter(private val interfaceType: InterfaceType) : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.inspect {
            schemaDefinitions {
                (schemaDefinition.typeDefinition as? EnumTypeDefinition)?.apply { emitModelFile().writeFile() }
            }
        }
    }

    private fun EnumTypeDefinition.emitModelFile() = kotlinFile(modelName) {
        kotlinEnum(fileName) {
            kotlinMember(
                "value".variableName(),
                baseType.typeName(),
                accessModifier = null
            )
            items.forEach {
                kotlinEnumItem(it.name, it.value)
            }
        }
    }

}