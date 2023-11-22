package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.validation.Validation

data class ObjectSchemaDefinition(
    override val originPath: String,
    override val description: String?,
    override val nullable: Boolean,
    override val properties: List<Pair<String, SchemaProperty>>,
    override val validations: List<Validation>
) : SchemaDefinition, Schema.ObjectSchema

data class ObjectSchemaReference(
    override val originPath: String,
    override val targetName: String,
    override val target: Schema.ObjectSchema,
    private val descriptionOverride: String? = null
) : SchemaReference<Schema.ObjectSchema>, Schema.ObjectSchema by target {

    override val description: String?
        get() = descriptionOverride ?: target.description

}
