package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

object TypeDefinitionHint : Hint<TypeDefinition> {

    var TransformableSchemaDefinition.typeDefinition: TypeDefinition
        get() = get(TypeDefinitionHint) ?: ProbableBug("No type defined for schema definition")
        set(value) = set(TypeDefinitionHint, value)

    val TransformableSchemaUsage.typeDefinition: TypeDefinition
        get() = schemaDefinition.typeDefinition

}