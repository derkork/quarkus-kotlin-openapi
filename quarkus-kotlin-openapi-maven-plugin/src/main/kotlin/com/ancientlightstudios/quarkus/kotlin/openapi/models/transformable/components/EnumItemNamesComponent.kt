package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchema

class EnumItemNamesComponent(val values: Map<String, String>) : SchemaComponent, MetaComponent {

    companion object {

        fun TransformableSchema.enumItemNamesComponent() =
            components.filterIsInstance<EnumItemNamesComponent>().firstOrNull()

    }

}