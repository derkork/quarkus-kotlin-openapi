package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.parameter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.Schema

data class QueryParameterDefinition(
    override val name: String,
    override val schema: Schema,
    override val description: String?,
    override val deprecated: Boolean,
    override val required: Boolean
) : ParameterDefinition, Parameter.QueryParameter

data class QueryParameterReference(
    override val targetName: String,
    override val target: Parameter.QueryParameter,
    private val descriptionOverride: String? = null
) : ParameterReference<Parameter.QueryParameter>, Parameter.QueryParameter by target {

    override val description: String?
        get() = descriptionOverride ?: target.description

}
