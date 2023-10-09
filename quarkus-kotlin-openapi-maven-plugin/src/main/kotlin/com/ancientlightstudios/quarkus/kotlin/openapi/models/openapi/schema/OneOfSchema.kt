package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema

data class OneOfSchemaDefinition(
    override val description: String?,
    override val nullable: Boolean,
    override val schemas: List<Schema>,
    override val discriminator: String?
) : SchemaDefinition, Schema.OneOfSchema

data class OneOfSchemaReference(
    override val targetName: String,
    private val definition: Schema.OneOfSchema,
    private val descriptionOverride: String? = null
) : SchemaReference, Schema.OneOfSchema by definition {

    override val description: String?
        get() = descriptionOverride ?: definition.description

}
