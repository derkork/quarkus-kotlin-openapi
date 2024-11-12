package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.SchemaModifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class SchemaModifierComponent(val modifier: SchemaModifier) : SchemaComponent, MetaComponent {

    companion object {

        fun OpenApiSchema.schemaModifierComponent(): SchemaModifierComponent? {
            val components = components.filterIsInstance<SchemaModifierComponent>()

            if (components.isEmpty()) {
                return null
            }

            if (components.size == 1) {
                return components.first()
            }

            // all components must have the same modifier
            val modifier = components.mapTo(mutableSetOf()) { it.modifier }
            if (modifier.size == 1) {
                return components.first()
            }

            ProbableBug("multiple schema modifier components found at schema $originPath")
        }

    }

}