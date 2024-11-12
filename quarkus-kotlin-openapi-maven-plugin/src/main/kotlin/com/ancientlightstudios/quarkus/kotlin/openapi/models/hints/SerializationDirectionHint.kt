package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.Direction
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// specifies which is the serialization direction
object SerializationDirectionHint : Hint<Direction> {

    var OpenApiSpec.serializationDirection: Direction
        get() = get(SerializationDirectionHint) ?: ProbableBug("Serialization direction not set")
        set(value) = set(SerializationDirectionHint, value)

}