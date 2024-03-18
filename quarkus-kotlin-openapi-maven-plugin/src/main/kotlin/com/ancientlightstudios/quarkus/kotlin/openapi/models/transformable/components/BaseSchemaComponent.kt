package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchema

class BaseSchemaComponent(override var schema: TransformableSchema) : SchemaUsage, SchemaComponent,
    ReferencingComponent