package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class DefaultComponent(val default: String) : SchemaComponent, MetaComponent {

    companion object {

        fun TransformableSchema.defaultComponent(): DefaultComponent? {
            val components = components.filterIsInstance<DefaultComponent>()
            return when {
                components.isEmpty() -> null
                components.size > 1 -> ProbableBug("Multiple instances of default component found at schema $originPath")
                else -> components.first()
            }
        }

    }


}