package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema

data class AllOfSchemaDefinition(
    override val description: String?,
    override val nullable: Boolean,
    override val schemas: List<Schema>
) : SchemaDefinition, Schema.AllOfSchema

data class AllOfSchemaReference(
    override val targetName: String,
    private val target: Schema.AllOfSchema,
    private val descriptionOverride: String? = null
) : SchemaReference, Schema.AllOfSchema by target {

    override val description: String?
        get() = descriptionOverride ?: target.description

}
