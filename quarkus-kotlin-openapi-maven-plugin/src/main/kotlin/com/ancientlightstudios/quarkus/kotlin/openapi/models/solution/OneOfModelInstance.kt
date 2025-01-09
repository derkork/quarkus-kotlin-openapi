package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

data class OneOfModelInstance(
    val ref: OneOfModelClass,
    override val required: Boolean,
    override val nullable: Boolean
) : ModelInstance