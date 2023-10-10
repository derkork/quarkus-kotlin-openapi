package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.parameter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.Schema

data class PathParameterDefinition(
    override val name: String,
    override val schema: Schema,
    override val description: String?
) : ParameterDefinition, Parameter.PathParameter

data class PathParameterReference(
    override val targetName: String,
    private val target: Parameter.PathParameter,
    private val descriptionOverride: String? = null
) : ParameterReference, Parameter.PathParameter by target {

    override val description: String?
        get() = descriptionOverride ?: target.description

}
