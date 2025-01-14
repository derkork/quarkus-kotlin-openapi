package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

data class CollectionModelInstance(
    val items: ModelUsage,
    override val required: Boolean,
    override val nullable: Boolean
) : ModelInstance