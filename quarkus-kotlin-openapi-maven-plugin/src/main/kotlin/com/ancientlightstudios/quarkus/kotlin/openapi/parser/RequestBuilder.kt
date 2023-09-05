package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.Request
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.RequestMethod

class RequestBuilder(
    private val path: String, private val method: RequestMethod,
    private val node: ObjectNode, private val schemaRegistry: SchemaRegistry
) {

    private val operationId = node.getTextOrNull("operationId") ?: "${method.name} $path"

    fun build(): Request {
        val parameters = buildParameters(schemaRegistry)
        val bodyType = buildBodyType(schemaRegistry)
        val returnType = buildReturnType(schemaRegistry)

        return Request(path, method, operationId, parameters, bodyType, returnType)
    }

    private fun buildParameters(schemaRegistry: SchemaRegistry) =
        node.withArray("parameters").map { it.parseAsRequestParameter(schemaRegistry, operationId) }

    private fun buildBodyType(schemaRegistry: SchemaRegistry) =
        // TODO: handle other content types when needed, then again, we don't need them for now
        (node.resolvePath("requestBody/content/application\\/json/schema") as? ObjectNode)
            ?.extractSchemaRef(schemaRegistry) { "$operationId Body" }

    private fun buildReturnType(schemaRegistry: SchemaRegistry) =
        // TODO: handle other content types when needed, then again, we don't need them for now
        (node.resolvePath("responses/200/content/application\\/json/schema") as? ObjectNode)
            ?.extractSchemaRef(schemaRegistry) { "$operationId Response" }
}

fun JsonNode.parseAsRequest(path: String, method: RequestMethod, schemaRegistry: SchemaRegistry): Request {
    require(this.isObject) { "Json object expected" }

    return RequestBuilder(path, method, this as ObjectNode, schemaRegistry).build()
}