package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class ArrayItemsComponent(override var schema: TransformableSchema) : SchemaUsage, SchemaComponent,
    StructuralComponent {

    companion object {

        fun TransformableSchema.arrayItemsComponent(): ArrayItemsComponent? {
            val components = components.filterIsInstance<ArrayItemsComponent>()
            return when {
                components.isEmpty() -> null
                components.size > 1 -> ProbableBug("Multiple instances of arrayItems component found at schema $originPath")
                else -> components.first()
            }
        }

    }

}
