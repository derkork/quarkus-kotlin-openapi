package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema

class ContainerModelNameComponent(val value: String) : SchemaComponent, MetaComponent {

    companion object {

        fun OpenApiSchema.containerModelNameComponent() =
            components.filterIsInstance<ContainerModelNameComponent>().firstOrNull()

    }

}