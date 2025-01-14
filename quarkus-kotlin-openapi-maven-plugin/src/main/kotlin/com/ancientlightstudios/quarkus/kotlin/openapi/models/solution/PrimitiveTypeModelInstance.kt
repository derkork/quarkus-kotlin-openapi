package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.BaseType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.SchemaValidation

data class PrimitiveTypeModelInstance(
    val itemType: BaseType,
    val defaultValue: String?,
    override val required: Boolean,
    override val nullable: Boolean,
    override val validations: List<SchemaValidation>
) : ModelInstance

