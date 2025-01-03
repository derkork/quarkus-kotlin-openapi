package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

class CollectionModelUsage(val items: ModelUsage, override val required: Boolean, override val nullable: Boolean) :
    ModelUsage