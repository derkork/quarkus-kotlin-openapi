package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Feature
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaDirection
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema

class OneOfModelClass(
    name: ComponentName,
    override val direction: SchemaDirection,
    override val source: OpenApiSchema
) : SolutionFile(name), ModelClass {

    override val features = mutableSetOf<Feature>()

    val options = mutableListOf<OneOfModelOption>()
    var discriminator: OneOfModelDiscriminator? = null

}

class OneOfModelOption(var name: ComponentName, val model: ModelUsage, val source: OpenApiSchema, var aliases: List<String>, var enforceAliasValue: Boolean)

class OneOfModelDiscriminator(val name: String, val sourceName: String)
