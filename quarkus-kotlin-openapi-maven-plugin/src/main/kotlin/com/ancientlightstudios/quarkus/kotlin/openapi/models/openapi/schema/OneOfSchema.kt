package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema

data class OneOfSchemaDefinition(
    override val description: String?,
    override val nullable: Boolean,
    override val schemas: List<Schema>,
    override val discriminator: String?
) : SchemaDefinition, Schema.OneOfSchema

data class OneOfSchemaReference(
    override val targetName: String,
    override val target: Schema.OneOfSchema,
    private val descriptionOverride: String? = null
) : SchemaReference<Schema.OneOfSchema>, Schema.OneOfSchema by target {

    override val description: String?
        get() = descriptionOverride ?: target.description

}
