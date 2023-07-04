data class ApiSpec(
    val requests:Set<Request>,
    val schemas:Set<Schema>
)

data class Request(
    val uri:String,
    val method:String,
    val operationId:String?,
    val parameters:List<RequestParameter>
)

enum class ParameterKind {
    PATH,
    QUERY,
    HEADER,
    COOKIE;

    companion object {
        fun fromString(string:String):ParameterKind {
            return when(string) {
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
    val name:String,
    val kind:ParameterKind,
    val required:Boolean,
    val type:Schema
)

data class Schema(
    val id:String,
    val name:String,
    val properties:List<SchemaProperty>
)

data class SchemaRef(
    val id:String
)

data class SchemaProperty(
    val name:String,
    val type:SchemaRef,
    val required:Boolean,
    val isList:Boolean
)