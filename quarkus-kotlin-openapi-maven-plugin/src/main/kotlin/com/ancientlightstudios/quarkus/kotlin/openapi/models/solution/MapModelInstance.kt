package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

data class MapModelInstance(
    val items: ModelUsage,
    override val required: Boolean,
    override val nullable: Boolean
) : ModelInstance