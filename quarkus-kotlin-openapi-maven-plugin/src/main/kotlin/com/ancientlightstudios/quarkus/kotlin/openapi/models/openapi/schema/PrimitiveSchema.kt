package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.validation.Validation

data class PrimitiveSchemaDefinition(
    override val description: String?,
    override val nullable: Boolean,
    override val type: String,
    override val format: String?,
    override val defaultValue: String?,
    override val validation: Validation
) : SchemaDefinition, Schema.PrimitiveSchema

data class PrimitiveSchemaReference(
    override val targetName: String,
    override val target: Schema.PrimitiveSchema,
    private val descriptionOverride: String? = null
) : SchemaReference<Schema.PrimitiveSchema>, Schema.PrimitiveSchema by target {

    override val description: String?
        get() = descriptionOverride ?: target.description

}
