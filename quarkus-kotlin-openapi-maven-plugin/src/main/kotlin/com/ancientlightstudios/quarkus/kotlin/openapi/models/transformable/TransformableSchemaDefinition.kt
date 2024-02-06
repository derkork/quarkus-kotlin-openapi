package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.SchemaDefinitionComponent

class TransformableSchemaDefinition(
    var name: String,
    var components: List<SchemaDefinitionComponent> = listOf()
) : TransformableObject()



