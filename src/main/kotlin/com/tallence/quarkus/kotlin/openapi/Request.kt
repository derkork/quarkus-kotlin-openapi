package com.tallence.quarkus.kotlin.openapi


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
    val operationId: String?,
    val parameters: List<RequestParameter>
     // TODO: body
    // TODO: return type
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
    // TODO: isList?

)

sealed class Schema(val typeName: String) {

    class BasicTypeSchema(typeName: String) : Schema(typeName)
    class ComplexSchema(typeName: String, val properties: List<SchemaProperty>) : Schema(typeName)

}

data class SchemaRef(
    val id: String
)

data class SchemaProperty(
    val name: String,
    val type: SchemaRef,
    val required: Boolean,
    val isList: Boolean
)