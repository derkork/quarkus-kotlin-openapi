package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchema

class ObjectValidationComponent(val required: List<String> = listOf()) : SchemaComponent, StructuralComponent {

    companion object {

        fun TransformableSchema.objectValidationComponent(): ObjectValidationComponent? {
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