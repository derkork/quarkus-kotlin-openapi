package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaTypes
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.SchemaDefinitionComponent.Companion.baseMerge
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.SchemaDefinitionComponent.Companion.extract

class TypeComponent(
    var types: List<SchemaTypes> = listOf(),
    var format: String? = null,
    var nullable: Boolean? = null
) : SchemaDefinitionComponent {

    companion object {

        fun List<TypeComponent>.merge() = baseMerge { component ->
            val types = component.extract { it.types } ?: emptyList()
            val format = component.extract { it.format }
            TypeComponent(types, format)
        }
    }

}