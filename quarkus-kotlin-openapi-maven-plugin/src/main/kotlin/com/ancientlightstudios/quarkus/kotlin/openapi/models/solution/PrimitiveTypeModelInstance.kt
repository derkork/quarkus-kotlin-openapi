package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.BaseType

data class PrimitiveTypeModelInstance(
    val itemType: BaseType,
    val defaultValue: String?,
    override val required: Boolean,
    override val nullable: Boolean
) : ModelInstance

