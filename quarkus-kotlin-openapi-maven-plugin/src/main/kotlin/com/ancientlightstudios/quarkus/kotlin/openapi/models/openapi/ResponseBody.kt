package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.Schema

// TODO: support different media-types (xml, file etc)
sealed interface ResponseBody {

    val schema: Schema
    val description: String?
    val headers: List<Pair<String, ResponseHeader>>
}

data class ResponseBodyDefinition(
    override val schema: Schema,
    override val description: String?,
    override val headers: List<Pair<String, ResponseHeader>>
) : ResponseBody

data class ResponseBodyReference(
    val targetName: String,
    private val definition: ResponseBody,
    private val descriptionOverride: String? = null
) : ResponseBody by definition {

    override val description: String?
        get() = descriptionOverride ?: definition.description

}
