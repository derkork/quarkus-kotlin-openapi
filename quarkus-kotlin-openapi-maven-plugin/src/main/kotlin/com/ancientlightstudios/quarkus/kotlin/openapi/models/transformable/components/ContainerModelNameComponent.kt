package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchema

class ContainerModelNameComponent(val value: String) : SchemaComponent, MetaComponent {

    companion object {

        fun TransformableSchema.containerModelNameComponent() =
            components.filterIsInstance<ContainerModelNameComponent>().firstOrNull()

    }

}