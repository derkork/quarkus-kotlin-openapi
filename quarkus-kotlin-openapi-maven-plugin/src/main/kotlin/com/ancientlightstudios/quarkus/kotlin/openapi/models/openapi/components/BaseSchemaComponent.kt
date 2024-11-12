package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.SchemaUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class BaseSchemaComponent(override var schema: OpenApiSchema) : SchemaUsage, SchemaComponent,
    ReferencingComponent {

    companion object {

        // fun TransformableSchema.baseSchemaComponents() = components.filterIsInstance<BaseSchemaComponent>()

        // TODO: replace this with the function above once we know how to merge multiple schemas (also includes fragments)
        fun OpenApiSchema.baseSchemaComponent(): BaseSchemaComponent? {
            val components = components.filterIsInstance<BaseSchemaComponent>()
            return when {
                components.isEmpty() -> null
                components.size > 1 -> ProbableBug("Multiple instances of base schema component found at schema $originPath")
                else -> components.first()
            }
        }

    }

}