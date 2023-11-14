package com.ancientlightstudios.quarkus.kotlin.openapi.utils

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.BooleanExpression.Companion.booleanExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.DoubleExpression.Companion.doubleExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.FloatExpression.Companion.floatExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.IntExpression.Companion.intExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.LongExpression.Companion.longExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression.Companion.stringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.UIntExpression.Companion.uIntExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.ULongExpression.Companion.uLongExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.rawClassName

fun ClassName.valueExpression(value: String) = when (this.render()) {
    "String" -> value.stringExpression()
    "Float" -> value.floatExpression()
    "Double" -> value.doubleExpression()
    "Int" -> value.intExpression()
    "UInt" -> value.uIntExpression()
    "Long" -> value.longExpression()
    "ULong" -> value.uLongExpression()
    "Boolean" -> value.booleanExpression()
    else -> throw IllegalArgumentException("unsupported primitive type ${this.render()}")
}

fun primitiveTypeFor(type: String, format: String?) = when {
    type == "string" -> "String"
    type == "number" && format == "float" -> "Float"
    type == "number" && (format == "double" || format == null) -> "Double"
    (type == "number" || type == "integer") && format == "int64" -> "Long"
    type == "integer" && (format == null || format == "int32" || format == "int16") -> "Int"
    (type == "number" || type == "integer") && (format == "uint32" || format == "uint16") -> "UInt"
    (type == "number" || type == "integer") && (format == "uint64") -> "ULong"
    type == "boolean" -> "Boolean"
    else -> throw IllegalArgumentException("unsupported primitive type mapping '$type' with format '$format'")
}.rawClassName()
