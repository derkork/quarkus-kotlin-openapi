package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema

class EnumItemNamesComponent(val values: Map<String, String>) : SchemaComponent, MetaComponent {

    companion object {

        fun OpenApiSchema.enumItemNamesComponent() =
            components.filterIsInstance<EnumItemNamesComponent>().firstOrNull()

    }

}