package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaUsage

class BaseDefinitionComponent(val innerSchema: TransformableSchemaUsage) : SchemaDefinitionComponent,
    ReferencingComponent