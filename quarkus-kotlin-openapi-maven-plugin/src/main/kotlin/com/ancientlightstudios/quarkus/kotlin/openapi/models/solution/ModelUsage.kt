package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

data class ModelUsage(val instance: ModelInstance, val overrideNullableWith: Boolean? = null) {

    fun acceptNull() = ModelUsage(instance, true)

    fun rejectNull() = ModelUsage(instance, false)

    fun isNullable() = when (overrideNullableWith) {
        null -> instance.isNullable()
        else -> overrideNullableWith
    }

}