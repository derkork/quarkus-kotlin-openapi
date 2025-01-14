package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.SchemaValidation

data class OneOfModelInstance(
    val ref: OneOfModelClass,
    override val required: Boolean,
    override val nullable: Boolean,
    override val validations: List<SchemaValidation>
) : ModelInstance