package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema

data class ArraySchemaDefinition(
    override val description: String?,
    override val nullable: Boolean,
    override val itemSchema: Schema
) : SchemaDefinition, Schema.ArraySchema

data class ArraySchemaReference(
    override val targetName: String,
    private val definition: Schema.ArraySchema,
    private val descriptionOverride: String? = null
) : SchemaReference, Schema.ArraySchema by definition {

    override val description: String?
        get() = descriptionOverride ?: definition.description

}
