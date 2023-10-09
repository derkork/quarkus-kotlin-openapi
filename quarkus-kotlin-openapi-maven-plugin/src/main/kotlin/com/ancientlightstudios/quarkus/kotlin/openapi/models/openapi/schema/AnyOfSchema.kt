package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema

data class AnyOfSchemaDefinition(
    override val description: String?,
    override val nullable: Boolean,
    override val schemas: List<Schema>
) : SchemaDefinition, Schema.AnyOfSchema

data class AnyOfSchemaReference(
    override val targetName: String,
    private val definition: Schema.AnyOfSchema,
    private val descriptionOverride: String? = null
) : SchemaReference, Schema.AnyOfSchema by definition {

    override val description: String?
        get() = descriptionOverride ?: definition.description

}
