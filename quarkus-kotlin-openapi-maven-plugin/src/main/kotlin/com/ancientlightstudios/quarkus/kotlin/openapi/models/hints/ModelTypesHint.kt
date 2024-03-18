package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeDefinition

// specifies the real type definitions which are the base for this spec. Schemas contains bidirectional or unidirectional
// type definitions, but some of them are just overlays and some are not needed at all (e.g. object which builds on top
// of another object).
object ModelTypesHint : Hint<List<TypeDefinition>> {

    var TransformableSpec.modelTypes: List<TypeDefinition>
        get() = get(ModelTypesHint) ?: emptyList()
        set(value) = set(ModelTypesHint, value)

}