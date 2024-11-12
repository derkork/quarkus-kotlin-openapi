package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.SchemaUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class AnyOfComponent(override val schemas: List<SchemaUsage>) : SomeOfComponent {

    companion object {

        fun OpenApiSchema.anyOfComponent(): AnyOfComponent? {
            val components = components.filterIsInstance<AnyOfComponent>()
            return when {
                components.isEmpty() -> null
                components.size > 1 -> ProbableBug("Multiple instances of anyOf component found at schema $originPath")
                else -> components.first()
            }
        }

    }

}
