package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableObject
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// specifies where the object was found in the spec file
object OriginPathHint : Hint<String> {

    var TransformableObject.originPath: String
        get() = get(OriginPathHint) ?: ProbableBug("Origin path not set for model object")
        set(value) = set(OriginPathHint, value)

}