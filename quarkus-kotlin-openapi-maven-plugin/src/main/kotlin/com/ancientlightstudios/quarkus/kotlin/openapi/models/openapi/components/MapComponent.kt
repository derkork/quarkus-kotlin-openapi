package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.SchemaUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class MapComponent(override var schema: OpenApiSchema) : SchemaUsage, SchemaComponent,
    StructuralComponent {

    companion object {

        fun OpenApiSchema.mapComponent(): MapComponent? {
            val components = components.filterIsInstance<MapComponent>()
            return when {
                components.isEmpty() -> null
                components.size > 1 -> ProbableBug("Multiple instances of map component found at schema $originPath")
                else -> components.first()
            }
        }

    }

}
