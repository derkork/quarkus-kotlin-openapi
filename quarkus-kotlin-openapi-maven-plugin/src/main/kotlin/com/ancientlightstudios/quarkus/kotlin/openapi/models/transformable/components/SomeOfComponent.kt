package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaUsage

interface SomeOfComponent : SchemaDefinitionComponent {

    val schemas: List<TransformableSchemaUsage>

}