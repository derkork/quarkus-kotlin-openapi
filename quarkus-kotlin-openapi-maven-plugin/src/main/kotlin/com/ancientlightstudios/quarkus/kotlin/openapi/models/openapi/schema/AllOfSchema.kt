package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.validation.Validation

data class AllOfSchemaDefinition(
    override val originPath: String,
    override val description: String?,
    override val nullable: Boolean,
    override val schemas: List<Schema>,
    override val validations: List<Validation>
) : SchemaDefinition, Schema.AllOfSchema

data class AllOfSchemaReference(
    override val originPath: String,
    override val targetName: String,
    override val target: Schema.AllOfSchema,
    private val descriptionOverride: String? = null
) : SchemaReference<Schema.AllOfSchema>, Schema.AllOfSchema by target {

    override val description: String?
        get() = descriptionOverride ?: target.description

}
