package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.BaseType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaDirection
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema

class EnumModelClass(
    name: ComponentName,
    val itemType: BaseType,
    override val direction: SchemaDirection,
    override val source: OpenApiSchema
) : SolutionFile(name), ModelClass {

    val items = mutableListOf<EnumModelItem>()

    // features

}

class EnumModelItem(val name: String, val value: String)