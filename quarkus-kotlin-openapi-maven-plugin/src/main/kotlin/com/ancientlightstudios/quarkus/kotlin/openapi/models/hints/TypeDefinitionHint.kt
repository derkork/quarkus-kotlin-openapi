package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// specifies the type definition for a schema
object TypeDefinitionHint : Hint<TypeDefinition> {

    var TransformableSchemaDefinition.typeDefinition: TypeDefinition
        get() = get(TypeDefinitionHint) ?: ProbableBug("No type assigned to schema")
        set(value) = set(TypeDefinitionHint, value)

    val TransformableSchemaDefinition.hasTypeDefinition: Boolean
        get() = get(TypeDefinitionHint) != null

    val TransformableParameter.typeDefinition : TypeDefinition
        get() = schema.schemaDefinition.typeDefinition

    val ContentMapping.typeDefinition : TypeDefinition
        get() = schema.schemaDefinition.typeDefinition

    val TransformableSchemaProperty.typeDefinition : TypeDefinition
        get() = schema.schemaDefinition.typeDefinition


    val TransformableSchemaUsage.typeDefinition: TypeDefinition
        get() = schemaDefinition.typeDefinition

    val TransformableSchemaUsage.hasTypeDefinition: Boolean
        get() = schemaDefinition.hasTypeDefinition



}