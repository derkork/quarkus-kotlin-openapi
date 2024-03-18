package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeDefinition

// specifies the unidirectional type definition for the down direction of a schema. Defaults to the bidirectional type
// definition of the schema if not set
object DownTypeDefinitionHint : Hint<TypeDefinition> {

    var TransformableSchema.downTypeDefinition: TypeDefinition
        get() = get(DownTypeDefinitionHint) ?: typeDefinition
        set(value) = set(DownTypeDefinitionHint, value)

}