package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaUsage

interface SomeOfComponent : SchemaDefinitionComponent, ReferencingComponent {

    val schemas: List<TransformableSchemaUsage>

}