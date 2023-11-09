package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.validation.Validation

data class EnumSchemaDefinition(
    override val originPath: String,
    override val description: String?,
    override val nullable: Boolean,
    override val type: String,
    override val format: String?,
    override val values: List<String>,
    override val defaultValue: String?,
    override val validation: Validation
) : SchemaDefinition, Schema.EnumSchema

data class EnumSchemaReference(
    override val originPath: String,
    override val targetName: String,
    override val target: Schema.EnumSchema,
    private val descriptionOverride: String? = null,
    private val nullableOverride: Boolean? = null
) : SchemaReference<Schema.EnumSchema>, Schema.EnumSchema by target {

    override val description: String?
        get() = descriptionOverride ?: target.description

}
