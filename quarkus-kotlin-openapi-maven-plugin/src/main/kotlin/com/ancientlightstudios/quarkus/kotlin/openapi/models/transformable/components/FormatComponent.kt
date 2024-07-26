package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class FormatComponent(val format: String) : SchemaComponent, StructuralComponent {

    companion object {

        fun TransformableSchema.formatComponent(): FormatComponent? {
            val components = components.filterIsInstance<FormatComponent>()

            if (components.isEmpty()) {
                return null
            }

            if (components.size == 1) {
                return components.first()
            }

            // all components must have the same format
            val formats = components.mapTo(mutableSetOf()) { it.format }
            if (formats.size == 1) {
                return components.first()
            }

            ProbableBug("multiple format components found at schema $originPath")
        }

    }

}