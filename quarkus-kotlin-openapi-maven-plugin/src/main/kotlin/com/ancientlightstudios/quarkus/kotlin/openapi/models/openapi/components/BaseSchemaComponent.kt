package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class BaseSchemaComponent(override var schema: OpenApiSchema) : SchemaComponent, SchemaContainer, StructuralComponent {

    override fun merge(other: List<SchemaComponent>, origin: String): Pair<SchemaComponent, List<SchemaComponent>> {
        ProbableBug("base schema components should no longer be available when schema components are merged. Found in schema $origin")
    }

}