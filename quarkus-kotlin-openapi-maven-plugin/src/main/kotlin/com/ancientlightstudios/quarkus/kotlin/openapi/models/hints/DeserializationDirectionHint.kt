package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.Direction
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// specifies which is the deserialization direction
object DeserializationDirectionHint : Hint<Direction> {

    var OpenApiSpec.deserializationDirection: Direction
        get() = get(DeserializationDirectionHint) ?: ProbableBug("Deserialization direction not set")
        set(value) = set(DeserializationDirectionHint, value)

}