package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaTypes
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class TypeComponent(val type: SchemaTypes) : SchemaComponent, StructuralComponent {

    companion object {

        fun TransformableSchema.typeComponent(): TypeComponent? {
            val components = components.filterIsInstance<TypeComponent>()

            if (components.isEmpty()) {
                return null
            }

            if (components.size == 1) {
                return components.first()
            }

            // all components must have the same type
            val types = components.mapTo(mutableSetOf()) { it.type }
            if (types.size == 1) {
                return components.first()
            }

            ProbableBug("multiple type components found at schema $originPath")
        }

    }

}