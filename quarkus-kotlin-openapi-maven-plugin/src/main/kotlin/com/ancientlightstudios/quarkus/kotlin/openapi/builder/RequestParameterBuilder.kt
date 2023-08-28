package com.ancientlightstudios.quarkus.kotlin.openapi.builder

import com.ancientlightstudios.quarkus.kotlin.openapi.ParameterKind
import com.ancientlightstudios.quarkus.kotlin.openapi.RequestParameter
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ancientlightstudios.quarkus.kotlin.openapi.getAsObjectNode
import com.ancientlightstudios.quarkus.kotlin.openapi.getBooleanOrNull
import com.ancientlightstudios.quarkus.kotlin.openapi.getTextOrNull

class RequestParameterBuilder(
    private val node: ObjectNode,
    private val schemaRegistry: SchemaRegistry,
    private val operationId: String
) {

    fun build(): RequestParameter {
        val name = node.getTextOrNull("name") ?: throw IllegalArgumentException("Parameter has no name")
        val kind = node.getTextOrNull("in") ?: throw IllegalArgumentException("Parameter $name has no 'in' property")

        val type = node.getAsObjectNode("schema").extractSchemaRef(schemaRegistry) { "$operationId $name" }
        val required = node.getBooleanOrNull("required") ?: false

        return RequestParameter(name, ParameterKind.fromString(kind), required, type)
    }

}

fun JsonNode.parseAsRequestParameter(schemaRegistry: SchemaRegistry, operationId: String): RequestParameter {
    require(this.isObject) { "Json object expected" }

    return RequestParameterBuilder(this as ObjectNode, schemaRegistry, operationId).build()
}
