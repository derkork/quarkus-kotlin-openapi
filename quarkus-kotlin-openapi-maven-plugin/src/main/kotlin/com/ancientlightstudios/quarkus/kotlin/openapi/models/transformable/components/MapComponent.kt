package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class MapComponent(override var schema: TransformableSchema) : SchemaUsage, SchemaComponent,
    StructuralComponent {

    companion object {

        fun TransformableSchema.mapComponent(): MapComponent? {
            val components = components.filterIsInstance<MapComponent>()
            return when {
                components.isEmpty() -> null
                components.size > 1 -> ProbableBug("Multiple instances of map component found at schema $originPath")
                else -> components.first()
            }
        }

    }

}
