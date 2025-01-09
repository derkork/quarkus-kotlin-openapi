package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaDirection
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema

class ObjectModelClass(
    name: ComponentName,
    override val direction: SchemaDirection,
    override val source: OpenApiSchema
) : SolutionFile(name), ModelClass {

    val properties = mutableListOf<ObjectModelProperties>()
    var additionalProperties: ModelInstance? = null

    // features

}

class ObjectModelProperties(val name: String, val sourceName: String, val model: ModelInstance)