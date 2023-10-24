package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.validation.Validation

data class ArraySchemaDefinition(
    override val description: String?,
    override val nullable: Boolean,
    override val itemSchema: Schema,
    override val validation: Validation
) : SchemaDefinition, Schema.ArraySchema

data class ArraySchemaReference(
    override val targetName: String,
    override val target: Schema.ArraySchema,
    private val descriptionOverride: String? = null
) : SchemaReference<Schema.ArraySchema>, Schema.ArraySchema by target {

    override val description: String?
        get() = descriptionOverride ?: target.description

}
