package com.tallence.quarkus.kotlin.openapi.builder

import com.tallence.quarkus.kotlin.openapi.ParameterKind
import com.tallence.quarkus.kotlin.openapi.RequestParameter
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.tallence.quarkus.kotlin.openapi.getAsObjectNode
import com.tallence.quarkus.kotlin.openapi.getBooleanOrNull
import com.tallence.quarkus.kotlin.openapi.getTextOrNull

class RequestParameterBuilder(private val node: ObjectNode, private val schemaRegistry: SchemaRegistry) {

    fun build(): RequestParameter {
        val name = node.getTextOrNull("name") ?: throw IllegalArgumentException("Parameter has no name")
        val kind = node.getTextOrNull("in") ?: throw IllegalArgumentException("Parameter $name has no 'in' property")

        val type = node.getAsObjectNode("schema").extractSchemaRef(schemaRegistry)
        val required = node.getBooleanOrNull("required") ?: false

        return RequestParameter(name, ParameterKind.fromString(kind), required, type)
    }

}

fun JsonNode.parseAsRequestParameter(schemaRegistry: SchemaRegistry): RequestParameter {
    if (!this.isObject) {
        throw IllegalArgumentException("Json object expected")
    }

    return RequestParameterBuilder(this as ObjectNode, schemaRegistry).build()
}
