package com.ancientlightstudios.quarkus.kotlin.openapi.utils

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.BooleanExpression.Companion.booleanExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.DoubleExpression.Companion.doubleExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.FloatExpression.Companion.floatExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.IntExpression.Companion.intExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.LongExpression.Companion.longExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression.Companion.stringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.*

fun ClassName.valueExpression(value: String) = when (this.render()) {
    "String" -> value.stringExpression()
    "Float" -> value.floatExpression()
    "Double" -> value.doubleExpression()
    "Int" -> value.intExpression()
    "Long" -> value.longExpression()
    "Boolean" -> value.booleanExpression()
    else -> throw IllegalArgumentException("unsupported primitive type ${this.render()}")
}

fun primitiveTypeFor(type: String, format: String?) = when {
    type == "string" -> "String"
    type == "number" && format == "float" -> "Float"
    type == "number" && format == "double" -> "Double"
    type == "integer" && format == "int64" -> "Long"
    type == "integer" && (format == null || format == "int32") -> "Int"
    type == "boolean" -> "Boolean"
    else -> throw IllegalArgumentException("unsupported primitive type mapping '$type' with format '$format'")
}.rawClassName()

fun TypeName.overrideWhenOptional(isOptional: Boolean): TypeName = when (this) {
    is TypeName.SimpleTypeName -> name.typeName(nullable || isOptional)
    is TypeName.GenericTypeName -> outerType.name.typeName(outerType.nullable || isOptional).of(innerType)
}

fun TypeDefinition.asUnsafePropertyType(): TypeName = when (this) {
    is InlinePrimitiveTypeDefinition -> "String".rawTypeName(true)
    is SharedPrimitiveTypeDefinition -> "String".rawTypeName(true)
    is EnumTypeDefinition -> "String".rawTypeName(true)
    is CollectionTypeDefinition -> "List".rawTypeName(true).of(innerType.asUnsafePropertyType())
    is ObjectTypeDefinition -> defaultType.extend(postfix = "Unsafe").overrideWhenOptional(true)
}
