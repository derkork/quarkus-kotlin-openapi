package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.validation.Validation

data class OneOfSchemaDefinition(
    override val originPath: String,
    override val description: String?,
    override val nullable: Boolean,
    override val schemas: Map<Schema, List<String>>,
    override val discriminator: String?,
    override val validation: Validation
) : SchemaDefinition, Schema.OneOfSchema

data class OneOfSchemaReference(
    override val originPath: String,
    override val targetName: String,
    override val target: Schema.OneOfSchema,
    private val descriptionOverride: String? = null
) : SchemaReference<Schema.OneOfSchema>, Schema.OneOfSchema by target {

    override val description: String?
        get() = descriptionOverride ?: target.description

}
