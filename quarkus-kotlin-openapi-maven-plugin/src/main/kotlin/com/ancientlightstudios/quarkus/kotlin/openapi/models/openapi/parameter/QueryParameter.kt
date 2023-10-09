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
    private val definition: Parameter.QueryParameter,
    private val descriptionOverride: String? = null
) : ParameterReference, Parameter.QueryParameter by definition {

    override val description: String?
        get() = descriptionOverride ?: definition.description

}
