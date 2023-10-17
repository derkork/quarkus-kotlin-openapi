package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinMethod
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.Expression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.*

fun KotlinMethod.addTransformStatement(
    parameterName: VariableName, typeDefinition: TypeDefinition, default: Expression?, required: Boolean,
    parameterContext: StringExpression, fromRequestBody: Boolean
): VariableName {
    val targetName = parameterName.extend(prefix = "maybe")

    val statement = when (typeDefinition) {
        is InlinePrimitiveTypeDefinition -> PrimitiveToMaybeTransformStatement(
            targetName,
            parameterName,
            parameterContext,
            typeDefinition.primitiveType,
            default,
            required
        )

        is SharedPrimitiveTypeDefinition -> PrimitiveToMaybeTransformStatement(
            targetName,
            parameterName,
            parameterContext,
            typeDefinition.name,
            default,
            required
        )

        is EnumTypeDefinition -> EnumToMaybeTransformStatement(
            targetName,
            parameterName,
            parameterContext,
            typeDefinition.name,
            default,
            required
        )

        is CollectionTypeDefinition -> if (fromRequestBody) {
            CollectionBodyToMaybeTransformStatement(
                targetName,
                parameterName,
                parameterContext,
                typeDefinition.defaultType,
                required
            ) {
                nestedTransformStatement(it, typeDefinition.innerType)
            }
        } else {
            CollectionPropertyToMaybeTransformStatement(targetName, parameterName, parameterContext, required) {
                nestedTransformStatement(it, typeDefinition.innerType)
            }
        }

        is ObjectTypeDefinition -> if (fromRequestBody) {
            ObjectBodyToMaybeTransformStatement(
                targetName,
                parameterName,
                parameterContext,
                typeDefinition.name.extend(postfix = "Unsafe"),
                required
            )
        } else {
            ObjectPropertyToMaybeTransformStatement(
                targetName,
                parameterName,
                parameterContext,
                typeDefinition.name.extend(postfix = "Unsafe"),
                required
            )
        }
    }

    addStatement(statement)
    return targetName
}

private fun nestedTransformStatement(
    sourceName: VariableName,
    type: TypeDefinition
): KotlinStatement {
    return when (type) {
        is InlinePrimitiveTypeDefinition -> NestedPrimitiveTransformStatement(
            sourceName,
            null,
            type.primitiveType,
            null,
            !type.isNullable
        )

        is SharedPrimitiveTypeDefinition -> NestedPrimitiveTransformStatement(
            sourceName,
            null,
            type.name,
            null,
            !type.isNullable
        )

        is EnumTypeDefinition -> NestedEnumTransformStatement(sourceName, null, type.name, null, !type.isNullable)
        is CollectionTypeDefinition -> NestedCollectionTransformStatement(sourceName, !type.isNullable) {
            nestedTransformStatement(it, type.innerType)
        }

        is ObjectTypeDefinition -> NestedObjectTransformStatement(
            sourceName,
            type.name.extend(postfix = "Unsafe"),
            !type.isNullable
        )
    }
}