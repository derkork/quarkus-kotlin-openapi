package com.ancientlightstudios.quarkus.kotlin.openapi.writer

import com.ancientlightstudios.quarkus.kotlin.openapi.Schema
import com.ancientlightstudios.quarkus.kotlin.openapi.SchemaProperty
import com.ancientlightstudios.quarkus.kotlin.openapi.SchemaRef
import com.ancientlightstudios.quarkus.kotlin.openapi.builder.SchemaRegistry
import java.util.*

fun Schema.toKotlinType(safe: Boolean): String {
    return when (this) {
        is Schema.PrimitiveTypeSchema -> {
            when (this.typeName) {
                "string" -> "String"
                "integer" -> "Int"
                "number" -> "Double"
                "boolean" -> "Boolean"
                "array" -> "List<Any>" // TODO
                else -> throw IllegalArgumentException("Unknown basic type: $typeName")
            }
        }

        is Schema.EnumSchema -> typeName.substringAfterLast("/").toKotlinClassName()
        else -> typeName.substringAfterLast("/").toKotlinClassName() + if (!safe) "Unsafe" else ""
    }
}

fun String.toKotlinClassName(): String {
    return this.toKotlinIdentifier()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }
}

fun String.toKotlinIdentifier(): String {
    // replace anything that is not a letter or a number with an underscore
    val cleaned = this.replace(Regex("[^a-zA-Z0-9]"), "_")

    // if it does not start with a letter, prepend an underscore
    return if (cleaned[0].isLetter()) cleaned else "_$cleaned"
}

fun SchemaRegistry.getPropertiesOf(list: List<SchemaRef>): List<SchemaProperty> {

    val result = mutableListOf<SchemaProperty>()

    for (ref in list) {
        val schema = this.resolve(ref)
        if (schema is Schema.ObjectTypeSchema) {
            result.addAll(schema.properties)
        }

        if (schema is Schema.OneOfSchema) {
            result.addAll(getPropertiesOf(schema.oneOf))
        }

        if (schema is Schema.AnyOfSchema) {
            result.addAll(getPropertiesOf(schema.anyOf))
        }

        if (schema is Schema.AllOfSchema) {
            result.addAll(getPropertiesOf(schema.allOf))
        }

    }

    return result
}