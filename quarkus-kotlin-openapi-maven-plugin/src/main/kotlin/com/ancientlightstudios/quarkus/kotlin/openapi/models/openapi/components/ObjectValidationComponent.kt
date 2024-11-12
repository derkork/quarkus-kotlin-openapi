package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema

class ObjectValidationComponent(val required: List<String> = listOf()) : SchemaComponent, StructuralComponent {

    companion object {

        fun OpenApiSchema.objectValidationComponent(): ObjectValidationComponent? {
            val components = components.filterIsInstance<ObjectValidationComponent>()

            if (components.isEmpty()) {
                return null
            }

            if (components.size == 1) {
                return components.first()
            }

            return ObjectValidationComponent(
                components.flatMapTo(mutableListOf()) { it.required }
            )
        }

    }

}