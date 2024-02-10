package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// specifies the type definition for a schema
object TypeDefinitionHint : Hint<TypeDefinition> {

    var TransformableSchemaDefinition.typeDefinition: TypeDefinition
        get() = get(TypeDefinitionHint) ?: ProbableBug("No type assigned to schema")
        set(value) = set(TypeDefinitionHint, value)

    val TransformableSchemaDefinition.hasTypeDefinition: Boolean
        get() = get(TypeDefinitionHint) != null

    val TransformableSchemaUsage.typeDefinition: TypeDefinition
        get() = schemaDefinition.typeDefinition

    val TransformableSchemaUsage.hasTypeDefinition: Boolean
        get() = schemaDefinition.hasTypeDefinition


}