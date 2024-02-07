package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaUsage

object ForceNullableHint : Hint<Boolean> {

    val TransformableSchemaUsage.forceNullable: Boolean
        get() = get(ForceNullableHint) ?: false

    fun TransformableSchemaUsage.forceNullable() {
        set(ForceNullableHint, true)
    }

}