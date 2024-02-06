package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaUsage

class AnyOfComponent(var schemas: List<TransformableSchemaUsage>) : SchemaDefinitionComponent
