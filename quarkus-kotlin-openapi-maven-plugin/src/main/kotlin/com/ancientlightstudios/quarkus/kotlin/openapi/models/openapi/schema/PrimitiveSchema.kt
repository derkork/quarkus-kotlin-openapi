package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema

data class PrimitiveSchemaDefinition(
    override val description: String?,
    override val nullable: Boolean,
    override val type: String,
    override val format: String?,
    override val defaultValue: String?
) : SchemaDefinition, Schema.PrimitiveSchema

data class PrimitiveSchemaReference(
    override val targetName: String,
    private val target: Schema.PrimitiveSchema,
    private val descriptionOverride: String? = null
) : SchemaReference, Schema.PrimitiveSchema by target {

    override val description: String?
        get() = descriptionOverride ?: target.description

}
