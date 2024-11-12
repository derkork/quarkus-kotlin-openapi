package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class EnumValidationComponent(val values: List<String>) : SchemaComponent, StructuralComponent {

    companion object {

        fun OpenApiSchema.enumValidationComponent() : EnumValidationComponent? {
            val components = components.filterIsInstance<EnumValidationComponent>()
            if (components.isEmpty()) {
                return null
            }

            if (components.size == 1) {
                return components.first()
            }

            // if there are multiple components of this type, we have to merge them by only keeping the common items of all components
            val enumItems = components.first().values.toMutableSet()
            components.drop(1).forEach {
                enumItems.retainAll(it.values.toSet())
            }

            if (enumItems.isEmpty()) {
                ProbableBug("no enum items remaining after merge in schema $originPath")
            }

            return EnumValidationComponent(enumItems.toList())
        }

    }

}
