import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.InputStream

fun read(inputStream: InputStream) = ObjectMapper(YAMLFactory()).readTree(inputStream) as ObjectNode

fun JsonNode?.getTextOrNull(property:String):String? = if (this != null && this.has(property)) this[property].asText() else null
fun JsonNode?.getBooleanOrFalse(property:String):Boolean = this != null && this.has(property) && this[property].asBoolean()



fun ObjectNode.parseAsApiSpec(): ApiSpec {
    val schemas = this.with("components").with("schemas").fields().asSequence().map {
        val schemaNode = it.value as ObjectNode
        val schemaName = it.key
        val schemaId = "#/components/schemas/$schemaName"
        val properties = schemaNode.with("properties").fields().asSequence().map { field ->
            val propertyName = field.key
            val propertyNode = field.value
            val propertySchema = propertyNode.getSchemaRef()
            val propertyRequired = propertyNode.getBooleanOrFalse("required")
            val propertyIsList = propertyNode.getBooleanOrFalse("isList")
            SchemaProperty(propertyName, propertyType, propertyRequired, propertyIsList)
        }.toSet()
        Schema(schemaId, schemaName, properties)
    }.toSet()

    val requests = this.with("paths").fields().asSequence().flatMap { (path, requests) ->
        requests.fields().asSequence().map { (method, request) ->
            val operationId = request.getTextOrNull("operationId")
            val parameters = request.get("parameters").parseParameters()
            Request(path, method, operationId, parameters)
        }
    }.toSet()
    return ApiSpec(requests, setOf())
}

fun JsonNode.getSchemaRef():SchemaRef {
    val schemaRef = this.getTextOrNull("\$ref")
     if (schemaRef != null) {
         return SchemaRef(schemaRef)
    }
    
}

fun JsonNode.parseParameters():List<RequestParameter> =
    if (this.isArray) {
        map { parameterNode ->
            val name = parameterNode.getTextOrNull("name") ?: throw IllegalArgumentException("Parameter has no name")
            val kind = parameterNode.getTextOrNull("in") ?: throw IllegalArgumentException("Parameter $name has no 'in' property")
            // TODO: $refs
            val type = parameterNode.get("schema").getTextOrNull("type") ?: throw IllegalArgumentException("Parameter $name has no schema.type property")
            val required = parameterNode.getBooleanOrFalse("required")
            RequestParameter(name, ParameterKind.fromString(kind), required, Schema(type))
        }
    } else {
        listOf()
    }
