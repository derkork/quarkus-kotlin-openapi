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
    override val target: Parameter.CookieParameter,
    private val descriptionOverride: String? = null
) : ParameterReference<Parameter.CookieParameter>, Parameter.CookieParameter by target {

    override val description: String?
        get() = descriptionOverride ?: target.description

}
