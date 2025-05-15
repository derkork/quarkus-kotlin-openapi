package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.SchemaValidation

data class ObjectModelInstance(
    val ref: ObjectModelClass,
    override val required: Boolean,
    override val nullable: Boolean,
    override val validations: List<SchemaValidation>
) : ModelInstance