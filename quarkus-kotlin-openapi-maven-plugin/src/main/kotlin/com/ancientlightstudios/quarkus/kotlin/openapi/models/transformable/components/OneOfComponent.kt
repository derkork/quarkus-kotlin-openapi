package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaUsage

class OneOfComponent(var schemas: List<TransformableSchemaUsage>) : SchemaDefinitionComponent

// TODO: discriminator, additionalMapping