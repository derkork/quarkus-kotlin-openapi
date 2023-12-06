package com.ancientlightstudios.quarkus.kotlin.openapi.utils

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
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
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.MethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.MethodName.Companion.rawMethodName

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

data class PrimitiveTypeInfo(val className: ClassName, val serializeMethodName:MethodName, val deserializeMethodName:MethodName)

private fun make(type:String, format: String?, customType:String?, defaultType:String) : PrimitiveTypeInfo {
    if (customType != null) {
        return PrimitiveTypeInfo(customType.rawClassName(), "from $type $format".methodName(), "as $type $format".methodName())
    }
    return PrimitiveTypeInfo(defaultType.rawClassName(), "from$defaultType".rawMethodName(), "as$defaultType".rawMethodName())
}


fun primitiveTypeFor(config: Config, type: String, format: String?): PrimitiveTypeInfo {
    if (type == "string") {
        return when {
            format.isNullOrEmpty() -> make(type, format, null, "String")
            else -> make(type, format, config.typeNameFor(type, format), "String")
        }
    }

    if (type == "boolean") {
        return when {
            format.isNullOrEmpty() -> make(type, format, null, "Boolean")
            else -> make(type, format, config.typeNameFor(type, format), "Boolean")
        }
    }

    if (type == "number") {
        return when (format) {
            "float" -> make(type, format, null, "Float")
            "double", null, "" -> make(type, format, null, "Double")
            "int32" -> make(type, format, null, "Int")
            "int64" -> make(type, format, null, "Long")
            "uint16", "uint32" -> make(type, format, null, "UInt")
            "uint64" -> make(type, format, null, "ULong")
            else -> make(type, format, config.typeNameFor(type, format), "Double")
        }
    }

    if (type == "integer") {
        return when (format) {
            "int32", null, "" -> make(type, format, null, "Int")
            "int64" -> make(type, format, null, "Long")
            "uint16", "uint32" -> make(type, format, null, "UInt")
            "uint64" -> make(type, format, null, "ULong")
            else -> make(type, format, config.typeNameFor(type, format), "Int")
        }
    }

    throw IllegalArgumentException("unsupported primitive type mapping '$type' with format '$format'")
}
