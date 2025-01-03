package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema

interface SomeOfComponent : SchemaComponent, StructuralComponent {

    val options: List<SomeOfOption>

}

class SomeOfOption(override var schema: OpenApiSchema) : SchemaContainer