package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema

data class ObjectSchemaDefinition(
    override val description: String?,
    override val nullable: Boolean,
    override val properties: List<Pair<String, SchemaProperty>>
) : SchemaDefinition, Schema.ObjectSchema

data class ObjectSchemaReference(
    override val targetName: String,
    private val definition: Schema.ObjectSchema,
    private val descriptionOverride: String? = null
) : SchemaReference, Schema.ObjectSchema by definition {

    override val description: String?
        get() = descriptionOverride ?: definition.description

}
