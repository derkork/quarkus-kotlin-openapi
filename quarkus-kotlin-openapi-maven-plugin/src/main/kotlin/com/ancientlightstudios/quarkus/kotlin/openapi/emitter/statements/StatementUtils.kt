package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinMethod
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.PathExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.PathExpression.Companion.pathExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.*

fun KotlinMethod.addTransformStatement(
    parameterName: VariableName, typeDefinitionUsage: TypeDefinitionUsage,
    parameterContext: StringExpression, fromRequestBody: Boolean
): VariableName {
    val targetName = parameterName.extend(prefix = "maybe")
    val source = parameterName.pathExpression()

    val statement = when (typeDefinitionUsage) {
        is InlinePrimitiveTypeUsage -> PrimitiveToMaybeTransformStatement(
            targetName,
            source,
            parameterContext,
            typeDefinitionUsage.primitiveTypeName,
            typeDefinitionUsage.defaultValue,
            !typeDefinitionUsage.nullable
        )

        is SharedPrimitiveTypeUsage -> PrimitiveToMaybeTransformStatement(
            targetName,
            source,
            parameterContext,
            typeDefinitionUsage.name,
            typeDefinitionUsage.defaultValue,
            !typeDefinitionUsage.nullable
        )

        is EnumTypeUsage -> EnumToMaybeTransformStatement(
            targetName,
            source,
            parameterContext,
            typeDefinitionUsage.name,
            typeDefinitionUsage.defaultValue,
            !typeDefinitionUsage.nullable
        )

        is CollectionTypeUsage -> if (fromRequestBody) {
            CollectionBodyToMaybeTransformStatement(
                targetName,
                source,
                parameterContext,
                typeDefinitionUsage.unsafeType,
                !typeDefinitionUsage.nullable
            ) {
                nestedTransformStatement(it, typeDefinitionUsage.innerType)
            }
        } else {
            CollectionPropertyToMaybeTransformStatement(
                targetName,
                source,
                parameterContext,
                !typeDefinitionUsage.nullable
            ) {
                nestedTransformStatement(it, typeDefinitionUsage.innerType)
            }
        }

        is ObjectTypeUsage -> if (fromRequestBody) {
            ObjectBodyToMaybeTransformStatement(
                targetName,
                source,
                parameterContext,
                typeDefinitionUsage.unsafeName,
                !typeDefinitionUsage.nullable
            )
        } else {
            ObjectPropertyToMaybeTransformStatement(
                targetName,
                source,
                parameterContext,
                typeDefinitionUsage.unsafeName,
                !typeDefinitionUsage.nullable
            )
        }
    }

    addStatement(statement)
    return targetName
}

fun getClientTransformStatement(
    parameterName: PathExpression, typeDefinitionUsage: TypeDefinitionUsage,
    parameterContext: StringExpression
): KotlinStatement {
    return when (typeDefinitionUsage) {
        is InlinePrimitiveTypeUsage -> PrimitiveToMaybeTransformStatement(
            null,
            parameterName,
            parameterContext,
            typeDefinitionUsage.primitiveTypeName,
            typeDefinitionUsage.defaultValue,
            !typeDefinitionUsage.nullable
        )

        is SharedPrimitiveTypeUsage -> PrimitiveToMaybeTransformStatement(
            null,
            parameterName,
            parameterContext,
            typeDefinitionUsage.name,
            typeDefinitionUsage.defaultValue,
            !typeDefinitionUsage.nullable
        )

        is EnumTypeUsage -> EnumToMaybeTransformStatement(
            null,
            parameterName,
            parameterContext,
            typeDefinitionUsage.name,
            typeDefinitionUsage.defaultValue,
            !typeDefinitionUsage.nullable
        )

        is CollectionTypeUsage -> CollectionBodyToMaybeTransformStatement(
            null,
            parameterName,
            parameterContext,
            typeDefinitionUsage.unsafeType,
            !typeDefinitionUsage.nullable
        ) {
            nestedTransformStatement(it, typeDefinitionUsage.innerType)
        }

        is ObjectTypeUsage -> ObjectBodyToMaybeTransformStatement(
            null,
            parameterName,
            parameterContext,
            typeDefinitionUsage.unsafeName,
            !typeDefinitionUsage.nullable
        )
    }
}

private fun nestedTransformStatement(
    sourceName: VariableName,
    typeUsage: TypeDefinitionUsage
): KotlinStatement {
    val source = sourceName.pathExpression()
    return when (typeUsage) {
        is InlinePrimitiveTypeUsage -> NestedPrimitiveTransformStatement(
            source, typeUsage.primitiveTypeName, !typeUsage.nullable
        )

        is SharedPrimitiveTypeUsage -> NestedPrimitiveTransformStatement(
            source, typeUsage.name, !typeUsage.nullable
        )

        is EnumTypeUsage -> NestedEnumTransformStatement(source, typeUsage.name, !typeUsage.nullable)
        is CollectionTypeUsage -> NestedCollectionTransformStatement(source, !typeUsage.nullable) {
            nestedTransformStatement(it, typeUsage.innerType)
        }

        is ObjectTypeUsage -> NestedObjectTransformStatement(
            source,
            typeUsage.name.extend(postfix = "Unsafe"),
            !typeUsage.nullable
        )
    }
}