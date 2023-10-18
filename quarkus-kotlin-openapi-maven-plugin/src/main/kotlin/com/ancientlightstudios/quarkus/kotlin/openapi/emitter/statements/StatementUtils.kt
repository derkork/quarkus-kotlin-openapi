package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinMethod
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.*

fun KotlinMethod.addTransformStatement(
    parameterName: VariableName, typeDefinitionUsage: TypeDefinitionUsage,
    parameterContext: StringExpression, fromRequestBody: Boolean
): VariableName {
    val targetName = parameterName.extend(prefix = "maybe")

    val statement = when (typeDefinitionUsage) {
        is InlinePrimitiveTypeUsage -> PrimitiveToMaybeTransformStatement(
            targetName,
            parameterName,
            parameterContext,
            typeDefinitionUsage.primitiveTypeName,
            typeDefinitionUsage.defaultValue,
            !typeDefinitionUsage.nullable
        )

        is SharedPrimitiveTypeUsage -> PrimitiveToMaybeTransformStatement(
            targetName,
            parameterName,
            parameterContext,
            typeDefinitionUsage.name,
            typeDefinitionUsage.defaultValue,
            !typeDefinitionUsage.nullable
        )

        is EnumTypeUsage -> EnumToMaybeTransformStatement(
            targetName,
            parameterName,
            parameterContext,
            typeDefinitionUsage.name,
            typeDefinitionUsage.defaultValue,
            !typeDefinitionUsage.nullable
        )

        is CollectionTypeUsage -> if (fromRequestBody) {
            CollectionBodyToMaybeTransformStatement(
                targetName,
                parameterName,
                parameterContext,
                typeDefinitionUsage.unsafeType,
                !typeDefinitionUsage.nullable
            ) {
                nestedTransformStatement(it, typeDefinitionUsage.innerType)
            }
        } else {
            CollectionPropertyToMaybeTransformStatement(
                targetName,
                parameterName,
                parameterContext,
                !typeDefinitionUsage.nullable
            ) {
                nestedTransformStatement(it, typeDefinitionUsage.innerType)
            }
        }

        is ObjectTypeUsage -> if (fromRequestBody) {
            ObjectBodyToMaybeTransformStatement(
                targetName,
                parameterName,
                parameterContext,
                typeDefinitionUsage.unsafeName,
                !typeDefinitionUsage.nullable
            )
        } else {
            ObjectPropertyToMaybeTransformStatement(
                targetName,
                parameterName,
                parameterContext,
                typeDefinitionUsage.unsafeName,
                !typeDefinitionUsage.nullable
            )
        }
    }

    addStatement(statement)
    return targetName
}

private fun nestedTransformStatement(
    sourceName: VariableName,
    typeUsage: TypeDefinitionUsage
): KotlinStatement {
    return when (typeUsage) {
        is InlinePrimitiveTypeUsage -> NestedPrimitiveTransformStatement(
            sourceName, typeUsage.primitiveTypeName, !typeUsage.nullable
        )

        is SharedPrimitiveTypeUsage -> NestedPrimitiveTransformStatement(
            sourceName, typeUsage.name, !typeUsage.nullable
        )

        is EnumTypeUsage -> NestedEnumTransformStatement(sourceName, typeUsage.name, !typeUsage.nullable)
        is CollectionTypeUsage -> NestedCollectionTransformStatement(sourceName, !typeUsage.nullable) {
            nestedTransformStatement(it, typeUsage.innerType)
        }

        is ObjectTypeUsage -> NestedObjectTransformStatement(
            sourceName,
            typeUsage.name.extend(postfix = "Unsafe"),
            !typeUsage.nullable
        )
    }
}