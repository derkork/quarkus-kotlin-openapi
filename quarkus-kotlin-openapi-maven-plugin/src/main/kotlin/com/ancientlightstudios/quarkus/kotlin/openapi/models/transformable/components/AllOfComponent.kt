package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class AllOfComponent(override val schemas: List<SchemaUsage>) : SomeOfComponent {

    companion object {

        fun TransformableSchema.allOfComponent(): AllOfComponent? {
            val components = components.filterIsInstance<AllOfComponent>()
            return when {
                components.isEmpty() -> null
                components.size > 1 -> ProbableBug("Multiple instances of allOf component found at schema $originPath")
                else -> components.first()
            }
        }

    }

}
