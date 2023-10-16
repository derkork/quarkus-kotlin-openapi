package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema

data class AnyOfSchemaDefinition(
    override val description: String?,
    override val nullable: Boolean,
    override val schemas: List<Schema.ObjectSchema>
) : SchemaDefinition, Schema.AnyOfSchema

data class AnyOfSchemaReference(
    override val targetName: String,
    override val target: Schema.AnyOfSchema,
    private val descriptionOverride: String? = null
) : SchemaReference<Schema.AnyOfSchema>, Schema.AnyOfSchema by target {

    override val description: String?
        get() = descriptionOverride ?: target.description

}
