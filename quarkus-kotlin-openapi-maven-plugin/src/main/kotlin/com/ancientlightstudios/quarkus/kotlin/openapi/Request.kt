package com.ancientlightstudios.quarkus.kotlin.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.builder.SchemaRegistry


data class ApiSpec(
    val requests: Set<Request>,
    val schemas: Set<Schema>
)

enum class RequestMethod {
    GET,
    POST,
    PUT,
    DELETE;

    companion object {
        fun fromString(string: String): RequestMethod {
            return when (string.lowercase()) {
                "get" -> GET
                "post" -> POST
                "put" -> PUT
                "delete" -> DELETE
                else -> throw IllegalArgumentException("Unknown request method $string")
            }
        }
    }
}

class Request(
    val path: String,
    val method: RequestMethod,
    val operationId: String,
    val parameters: List<RequestParameter>,
    val bodyType: SchemaRef?,
    val returnType: SchemaRef?
)

enum class ParameterKind {
    PATH,
    QUERY,
    HEADER,
    COOKIE;

    companion object {
        fun fromString(string: String): ParameterKind {
            return when (string) {
                "path" -> PATH
                "query" -> QUERY
                "header" -> HEADER
                "cookie" -> COOKIE
                else -> throw IllegalArgumentException("Unknown parameter kind $string")
            }
        }
    }
}

data class RequestParameter(
    val name: String,
    val kind: ParameterKind,
    val required: Boolean,
    val type: SchemaRef
)

sealed class Schema(val typeName: String) {

    class PrimitiveTypeSchema(typeName: String) : Schema(typeName)

    class ArraySchema(val items: SchemaRef) : Schema("array")
    class ObjectTypeSchema(typeName: String, val properties: List<SchemaProperty>) : Schema(typeName)

    class OneOfSchema(typeName: String, val discriminator:String, val oneOf: List<SchemaRef>) : Schema(typeName)
    class AnyOfSchema(typeName: String, val anyOf: List<SchemaRef>) : Schema(typeName)
    class AllOfSchema(typeName: String, val allOf: List<SchemaRef>) : Schema(typeName)
    class EnumSchema(typeName: String, val values: List<String>) : Schema(typeName)
}

data class SchemaRef(
    val id: String,
    private val schemaRegistry: SchemaRegistry
) {
    fun resolve(): Schema {
        return schemaRegistry.resolve(this)
    }
}

data class SchemaProperty(
    val name: String,
    val type: SchemaRef,
    val required: Boolean,
)