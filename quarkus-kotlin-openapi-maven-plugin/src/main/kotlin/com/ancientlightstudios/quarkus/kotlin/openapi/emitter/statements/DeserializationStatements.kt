package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.Expression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.PathExpression.Companion.pathExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.*

fun getDeserializationStatement(
    source: Expression, targetName: VariableName,
    typeDefinitionUsage: TypeDefinitionUsage,
    fromJsonNode: Boolean
) = when (typeDefinitionUsage) {
    is InlinePrimitiveTypeUsage -> PrimitiveDeserializationStatement(
        source, targetName, typeDefinitionUsage.primitiveTypeName, typeDefinitionUsage.defaultValue,
        !typeDefinitionUsage.nullable, typeDefinitionUsage.validation, typeDefinitionUsage.valueTransform
    )

    is EnumTypeUsage -> EnumDeserializationStatement(
        source, targetName, typeDefinitionUsage.name, typeDefinitionUsage.defaultValue,
        !typeDefinitionUsage.nullable, typeDefinitionUsage.validation, typeDefinitionUsage.valueTransform
    )

    is CollectionTypeUsage -> CollectionDeserializationStatement(
        source, targetName, !typeDefinitionUsage.nullable, typeDefinitionUsage.validation,
        typeDefinitionUsage.valueTransform, fromJsonNode
    ) {
        nestedDeserializationStatement(it, typeDefinitionUsage.innerType, fromJsonNode)
    }

    is ObjectTypeUsage -> ObjectDeserializationStatement(
        source, targetName, typeDefinitionUsage.unsafeName, !typeDefinitionUsage.nullable,
        typeDefinitionUsage.validation, typeDefinitionUsage.valueTransform
    )

    is AnyOfTypeUsage -> ObjectDeserializationStatement(
        source, targetName, typeDefinitionUsage.unsafeName, !typeDefinitionUsage.nullable,
        typeDefinitionUsage.validation, typeDefinitionUsage.valueTransform
    )

    is OneOfTypeUsage -> ObjectDeserializationStatement(
        source, targetName, typeDefinitionUsage.unsafeName, !typeDefinitionUsage.nullable,
        typeDefinitionUsage.validation, typeDefinitionUsage.valueTransform
    )
}

private fun nestedDeserializationStatement(
    sourceName: VariableName,
    typeUsage: TypeDefinitionUsage,
    fromJsonNode: Boolean
): KotlinStatement {
    val source = sourceName.pathExpression()
    return when (typeUsage) {
        is InlinePrimitiveTypeUsage -> NestedPrimitiveDeserializationStatement(
            source, typeUsage.primitiveTypeName, !typeUsage.nullable,
            typeUsage.validation, typeUsage.valueTransform
        )

        is EnumTypeUsage -> NestedEnumDeserializationStatement(
            source, typeUsage.name, !typeUsage.nullable, typeUsage.validation,
            typeUsage.valueTransform
        )

        is CollectionTypeUsage -> NestedCollectionDeserializationStatement(
            source, !typeUsage.nullable, typeUsage.validation, typeUsage.valueTransform, fromJsonNode
        ) {
            nestedDeserializationStatement(it, typeUsage.innerType, fromJsonNode)
        }

        is ObjectTypeUsage -> NestedObjectDeserializationStatement(
            source, typeUsage.unsafeName, !typeUsage.nullable,
            typeUsage.validation, typeUsage.valueTransform
        )

        is AnyOfTypeUsage -> NestedObjectDeserializationStatement(
            source, typeUsage.unsafeName, !typeUsage.nullable,
            typeUsage.validation, typeUsage.valueTransform
        )

        is OneOfTypeUsage -> NestedObjectDeserializationStatement(
            source, typeUsage.unsafeName, !typeUsage.nullable,
            typeUsage.validation, typeUsage.valueTransform
        )

    }
}