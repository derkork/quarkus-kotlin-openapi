package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.Schema

sealed interface ResponseHeader {

    val schema: Schema
    val description: String?
    val deprecated: Boolean
    val required: Boolean

}

data class ResponseHeaderDefinition(
    override val schema: Schema,
    override val description: String?,
    override val deprecated: Boolean,
    override val required: Boolean
) : ResponseHeader

data class ResponseHeaderReference(
    val targetName: String,
    private val definition: ResponseHeader,
    private val descriptionOverride: String? = null
) : ResponseHeader by definition {

    override val description: String?
        get() = descriptionOverride ?: definition.description

}
