package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.Kotlin
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaTypes
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class TypeMapper(private val config: Config) {

    fun mapToPrimitiveType(type: SchemaTypes, format: String?) = when (type) {
        SchemaTypes.String -> mapStringType(format)
        SchemaTypes.Number -> mapNumberType(format)
        SchemaTypes.Integer -> mapIntegerType(format)
        SchemaTypes.Boolean -> mapBooleanType(format)
        else -> ProbableBug("unsupported primitive type mapping '$type' with format '$format'")
    }

    private fun mapCustomType(type: SchemaTypes, format: String?, fallback: ClassName): ClassName {
        if (format.isNullOrEmpty()) {
            return fallback
        }

        return config.typeNameFor(type.value, format) ?: fallback
    }

    private fun mapStringType(format: String?) = when (format) {
        "binary" -> Kotlin.ByteArrayClass
        else -> mapCustomType(SchemaTypes.String, format, Kotlin.StringClass)
    }

    private fun mapNumberType(format: String?) = when (format) {
        "float" -> Kotlin.FloatClass
        "double" -> Kotlin.DoubleClass
        "int32" -> Kotlin.IntClass
        "int64" -> Kotlin.LongClass
        "uint16", "uint32" -> Kotlin.UIntClass
        "uint64" -> Kotlin.ULongClass
        else -> mapCustomType(SchemaTypes.Number, format, Kotlin.DoubleClass)
    }

    private fun mapIntegerType(format: String?) = when (format) {
        "int32" -> Kotlin.IntClass
        "int64" -> Kotlin.LongClass
        "uint16", "uint32" -> Kotlin.UIntClass
        "uint64" -> Kotlin.ULongClass
        else -> mapCustomType(SchemaTypes.Integer, format, Kotlin.IntClass)
    }

    private fun mapBooleanType(format: String?) = mapCustomType(SchemaTypes.Boolean, format, Kotlin.BooleanClass)

}