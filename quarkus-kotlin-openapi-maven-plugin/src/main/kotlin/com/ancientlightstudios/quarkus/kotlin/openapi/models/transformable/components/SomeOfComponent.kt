package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaUsage

interface SomeOfComponent : SchemaComponent, ReferencingComponent {

    val schemas: List<SchemaUsage>

}