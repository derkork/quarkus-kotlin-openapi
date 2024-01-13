package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableObject
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

object OriginPathHint : Hint<String> {

    fun TransformableObject.setOriginPath(value: String) {
        set(OriginPathHint, value)
    }

    fun TransformableObject.getOriginPath() = get(OriginPathHint) ?: ProbableBug("Origin path not set for model object")
}