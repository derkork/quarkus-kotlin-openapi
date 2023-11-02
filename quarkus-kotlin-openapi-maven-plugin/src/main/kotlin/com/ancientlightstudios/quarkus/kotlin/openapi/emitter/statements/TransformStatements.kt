package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.Expression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.PathExpression.Companion.pathExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.*

fun getTransformStatement(
    source: Expression, targetName: VariableName,
    typeDefinitionUsage: TypeDefinitionUsage,
    fromJsonNode: Boolean
) = when (typeDefinitionUsage) {
    is InlinePrimitiveTypeUsage -> PrimitiveTransformStatement(
        source, targetName, typeDefinitionUsage.primitiveTypeName, typeDefinitionUsage.defaultValue,
        !typeDefinitionUsage.nullable, typeDefinitionUsage.validation, typeDefinitionUsage.valueTransform
    )

    is EnumTypeUsage -> EnumTransformStatement(
        source, targetName, typeDefinitionUsage.name, typeDefinitionUsage.defaultValue,
        !typeDefinitionUsage.nullable, typeDefinitionUsage.validation, typeDefinitionUsage.valueTransform
    )

    is CollectionTypeUsage -> CollectionTransformStatement(
        source, targetName, !typeDefinitionUsage.nullable, typeDefinitionUsage.validation,
        typeDefinitionUsage.valueTransform, fromJsonNode
    ) {
        nestedTransformStatement(it, typeDefinitionUsage.innerType, fromJsonNode)
    }

    is ObjectTypeUsage -> ObjectTransformStatement(
        source, targetName, typeDefinitionUsage.unsafeName, !typeDefinitionUsage.nullable,
        typeDefinitionUsage.validation, typeDefinitionUsage.valueTransform
    )

    is AnyOfTypeUsage -> ObjectTransformStatement(
        source, targetName, typeDefinitionUsage.unsafeName, !typeDefinitionUsage.nullable,
        typeDefinitionUsage.validation, typeDefinitionUsage.valueTransform
    )

    is OneOfTypeUsage -> ObjectTransformStatement(
        source, targetName, typeDefinitionUsage.unsafeName, !typeDefinitionUsage.nullable,
        typeDefinitionUsage.validation, typeDefinitionUsage.valueTransform
    )
}

private fun nestedTransformStatement(
    sourceName: VariableName,
    typeUsage: TypeDefinitionUsage,
    fromJsonNode: Boolean
): KotlinStatement {
    val source = sourceName.pathExpression()
    return when (typeUsage) {
        is InlinePrimitiveTypeUsage -> NestedPrimitiveTransformStatement(
            source, typeUsage.primitiveTypeName, !typeUsage.nullable,
            typeUsage.validation, typeUsage.valueTransform
        )

        is EnumTypeUsage -> NestedEnumTransformStatement(
            source, typeUsage.name, !typeUsage.nullable, typeUsage.validation,
            typeUsage.valueTransform
        )

        is CollectionTypeUsage -> NestedCollectionTransformStatement(
            source, !typeUsage.nullable, typeUsage.validation, typeUsage.valueTransform, fromJsonNode
        ) {
            nestedTransformStatement(it, typeUsage.innerType, fromJsonNode)
        }

        is ObjectTypeUsage -> NestedObjectTransformStatement(
            source, typeUsage.unsafeName, !typeUsage.nullable,
            typeUsage.validation, typeUsage.valueTransform
        )

        is AnyOfTypeUsage -> NestedObjectTransformStatement(
            source, typeUsage.unsafeName, !typeUsage.nullable,
            typeUsage.validation, typeUsage.valueTransform
        )

        is OneOfTypeUsage -> NestedObjectTransformStatement(
            source, typeUsage.unsafeName, !typeUsage.nullable,
            typeUsage.validation, typeUsage.valueTransform
        )

    }
}