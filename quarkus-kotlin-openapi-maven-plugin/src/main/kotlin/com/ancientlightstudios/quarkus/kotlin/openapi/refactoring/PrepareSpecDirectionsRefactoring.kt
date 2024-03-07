package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.InterfaceType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.DeserializationDirectionHint.deserializationDirection
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SerializationDirectionHint.serializationDirection
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.Direction

class PrepareSpecDirectionsRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        if (interfaceType == InterfaceType.SERVER) {
            // server serialize the response and deserialize the request
            spec.serializationDirection = Direction.Down
            spec.deserializationDirection = Direction.Up
        } else {
            // clients serialize the request and deserialize the response
            spec.serializationDirection = Direction.Up
            spec.deserializationDirection = Direction.Down
        }
    }

}
