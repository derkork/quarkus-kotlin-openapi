package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.validation.Validation

data class AnyOfSchemaDefinition(
    override val originPath: String,
    override val description: String?,
    override val nullable: Boolean,
    override val schemas: List<Schema>,
    override val validations: List<Validation>
) : SchemaDefinition, Schema.AnyOfSchema

data class AnyOfSchemaReference(
    override val originPath: String,
    override val targetName: String,
    override val target: Schema.AnyOfSchema,
    private val descriptionOverride: String? = null
) : SchemaReference<Schema.AnyOfSchema>, Schema.AnyOfSchema by target {

    override val description: String?
        get() = descriptionOverride ?: target.description

}
