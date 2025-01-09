package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

data class ObjectModelInstance(
    val ref: ObjectModelClass,
    override val required: Boolean,
    override val nullable: Boolean
) : ModelInstance