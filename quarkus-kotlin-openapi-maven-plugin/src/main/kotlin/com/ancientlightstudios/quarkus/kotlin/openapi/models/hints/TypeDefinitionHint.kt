package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// specifies the bidirectional (or general) type definition of a schema. Some of these type definitions will be
// replaced by unidirectional (up/down) type definitions in case of a split (e.g. if they contain read-only/write-only
// properties or refer to such types)
object TypeDefinitionHint : Hint<TypeDefinition> {

    var OpenApiSchema.typeDefinition: TypeDefinition
        get() = get(TypeDefinitionHint) ?: ProbableBug("No type assigned to schema ${this.originPath}")
        set(value) = set(TypeDefinitionHint, value)

    val OpenApiSchema.hasTypeDefinition: Boolean
        get() = get(TypeDefinitionHint) != null

    fun OpenApiSchema.clearTypeDefinition() = clear(TypeDefinitionHint)

}