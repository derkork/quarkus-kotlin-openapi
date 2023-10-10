package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.parameter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.Schema

data class HeaderParameterDefinition(
    override val name: String,
    override val schema: Schema,
    override val description: String?,
    override val deprecated: Boolean,
    override val required: Boolean
) : ParameterDefinition, Parameter.HeaderParameter

data class HeaderParameterReference(
    override val targetName: String,
    private val target: Parameter.HeaderParameter,
    private val descriptionOverride: String? = null
) : ParameterReference, Parameter.HeaderParameter by target {

    override val description: String?
        get() = descriptionOverride ?: target.description

}
