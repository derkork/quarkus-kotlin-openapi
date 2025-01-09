package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

data class EnumModelInstance(
    val ref: EnumModelClass,
    val defaultValue: String?,
    override val required: Boolean,
    override val nullable: Boolean
) : ModelInstance