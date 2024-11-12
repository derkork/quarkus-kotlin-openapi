package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchemaProperty

class ObjectComponent(val properties: List<OpenApiSchemaProperty> = listOf()) : SchemaComponent,
    StructuralComponent {

    companion object {

        fun OpenApiSchema.objectComponent(): ObjectComponent? {
            val components = components.filterIsInstance<ObjectComponent>()

            if (components.isEmpty()) {
                return null
            }

            if (components.size == 1) {
                return components.first()
            }

            return ObjectComponent(
                components.flatMapTo(mutableListOf()) { it.properties }
            )
        }

    }

}