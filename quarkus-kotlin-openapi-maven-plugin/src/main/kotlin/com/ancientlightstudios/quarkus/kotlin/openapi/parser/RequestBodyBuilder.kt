package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.RequestBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ValidationInfo
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode

class RequestBodyBuilder(
    private val node: ObjectNode,
    private val schemaRegistry: SchemaRegistry,
    private val operationId: String
) {

    fun build(): RequestBody {
        val schemaNode = node.resolvePath("content/application\\/json/schema") as? ObjectNode
            ?: throw IllegalStateException("request body without schema")

        val required = node.getBooleanOrNull("required") ?: false
        val type = schemaNode.extractSchemaRef(schemaRegistry) { "$operationId Body" }

        return RequestBody(type, ValidationInfo(required))
    }

}

fun JsonNode.parseAsRequestBody(schemaRegistry: SchemaRegistry, operationId: String): RequestBody {
    require(this.isObject) { "Json object expected" }

    return RequestBodyBuilder(this as ObjectNode, schemaRegistry, operationId).build()
}
