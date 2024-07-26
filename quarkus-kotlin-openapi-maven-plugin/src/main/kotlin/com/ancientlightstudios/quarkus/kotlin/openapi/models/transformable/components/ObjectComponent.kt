package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaProperty

class ObjectComponent(val properties: List<TransformableSchemaProperty> = listOf()) : SchemaComponent,
    StructuralComponent {

    companion object {

        fun TransformableSchema.objectComponent(): ObjectComponent? {
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