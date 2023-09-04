package com.ancientlightstudios.quarkus.kotlin.openapi.writer

import com.ancientlightstudios.quarkus.kotlin.openapi.*
import com.ancientlightstudios.quarkus.kotlin.openapi.builder.SchemaRegistry
import java.util.*

fun Schema.toKotlinType(safeObject: Boolean, safeList: Boolean, listAsArray: Boolean = false): String {
    return when (this) {
        is Schema.PrimitiveTypeSchema -> {
            when (this.typeName) {
                "string" -> "String"
                "password" -> "String"
                "integer" -> "Int"
                "int32" -> "Int"
                "int64" -> "Long"
                "float" -> "Float"
                "number" -> "Double"
                "boolean" -> "Boolean"
                else -> throw IllegalArgumentException("Unknown basic type: $typeName")
            }
        }
        is Schema.ArraySchema -> {
            val type = if (listAsArray) "Array" else "List"
            val itemType = items.resolve().toKotlinType(safeObject, safeList, listAsArray)
            val unsafe = if (safeList) "" else "?"
            "$type<$itemType$unsafe>"
        }
        is Schema.EnumSchema -> typeName.substringAfterLast("/").toKotlinClassName() + if (!safeObject)  "Unsafe" else ""
        else -> typeName.substringAfterLast("/").toKotlinClassName() + if (!safeObject) "Unsafe" else ""
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

class InputInfo(val name: String, val contextPath: String, val type: SchemaRef, val required: Boolean)

class RequestInfo(val className:String, val inputInfo: List<InputInfo>) {
    fun hasInput() = inputInfo.isNotEmpty()
}

fun Request.asRequestInfo(): RequestInfo {
    val inputInfo = mutableListOf<InputInfo>()

    for (parameter in parameters) {
        inputInfo.add(InputInfo(parameter.name.toKotlinIdentifier(),
            "request.${parameter.kind.toString().lowercase()}.${parameter.name}",
            parameter.type, parameter.required))
    }

    if (bodyType != null) {
        inputInfo.add(InputInfo("body", "request.body", bodyType,
            true)) // TODO: extend body builder to read required flag
    }

    return RequestInfo(operationId.toKotlinClassName(), inputInfo)
}