package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema

data class ObjectSchemaDefinition(
    override val description: String?,
    override val nullable: Boolean,
    override val properties: List<Pair<String, SchemaProperty>>
) : SchemaDefinition, Schema.ObjectSchema

data class ObjectSchemaReference(
    override val targetName: String,
    private val target: Schema.ObjectSchema,
    private val descriptionOverride: String? = null
) : SchemaReference, Schema.ObjectSchema by target {

    override val description: String?
        get() = descriptionOverride ?: target.description

}
