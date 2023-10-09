package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.parameter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.Schema

data class CookieParameterDefinition(
    override val name: String,
    override val schema: Schema,
    override val description: String?,
    override val deprecated: Boolean,
    override val required: Boolean
) : ParameterDefinition, Parameter.CookieParameter

data class CookieParameterReference(
    override val targetName: String,
    private val definition: Parameter.CookieParameter,
    private val descriptionOverride: String? = null
) : ParameterReference, Parameter.CookieParameter by definition {

    override val description: String?
        get() = descriptionOverride ?: definition.description

}
