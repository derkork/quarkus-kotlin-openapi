package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.SchemaUsage

interface SomeOfComponent : SchemaComponent, ReferencingComponent {

    val schemas: List<SchemaUsage>

}