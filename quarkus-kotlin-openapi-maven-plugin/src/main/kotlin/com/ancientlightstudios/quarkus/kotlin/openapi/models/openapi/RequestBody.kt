package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.Schema

// TODO: support different media-types (xml, file etc)
sealed interface RequestBody {

    val schema: Schema
    val description: String?
    val required: Boolean

}

data class RequestBodyDefinition(
    override val schema: Schema,
    override val description: String?,
    override val required: Boolean
) : RequestBody

data class RequestBodyReference(
    val targetName: String,
    private val definition: RequestBody,
    private val descriptionOverride: String? = null
) : RequestBody by definition {

    override val description: String?
        get() = descriptionOverride ?: definition.description

}
