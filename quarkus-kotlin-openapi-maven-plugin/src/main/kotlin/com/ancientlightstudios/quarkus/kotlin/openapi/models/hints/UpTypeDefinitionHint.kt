package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeDefinition

// specifies the unidirectional type definition for the up direction of a schema. Defaults to the bidirectional type
// definition of the schema if not set
object UpTypeDefinitionHint : Hint<TypeDefinition> {

    var OpenApiSchema.upTypeDefinition: TypeDefinition
        get() = get(UpTypeDefinitionHint) ?: typeDefinition
        set(value) = set(UpTypeDefinitionHint, value)

}