package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.Direction
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// specifies which is the deserialization direction
object DeserializationDirectionHint : Hint<Direction> {

    var TransformableSpec.deserializationDirection: Direction
        get() = get(DeserializationDirectionHint) ?: ProbableBug("Deserialization direction not set")
        set(value) = set(DeserializationDirectionHint, value)

}