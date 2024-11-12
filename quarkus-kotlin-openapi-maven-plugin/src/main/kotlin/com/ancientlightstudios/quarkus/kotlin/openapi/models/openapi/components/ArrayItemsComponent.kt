package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.SchemaUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class ArrayItemsComponent(override var schema: OpenApiSchema) : SchemaUsage, SchemaComponent,
    StructuralComponent {

    companion object {

        fun OpenApiSchema.arrayItemsComponent(): ArrayItemsComponent? {
            val components = components.filterIsInstance<ArrayItemsComponent>()
            return when {
                components.isEmpty() -> null
                components.size > 1 -> ProbableBug("Multiple instances of arrayItems component found at schema $originPath")
                else -> components.first()
            }
        }

    }

}
