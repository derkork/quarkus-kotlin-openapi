package com.ancientlightstudios.quarkus.kotlin.openapi.builder

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ancientlightstudios.quarkus.kotlin.openapi.Request
import com.ancientlightstudios.quarkus.kotlin.openapi.RequestMethod
import com.ancientlightstudios.quarkus.kotlin.openapi.getTextOrNull
import com.ancientlightstudios.quarkus.kotlin.openapi.resolvePath

class RequestBuilder(
    private val path: String, private val method: RequestMethod,
    private val node: ObjectNode, private val schemaRegistry: SchemaRegistry
) {

    fun build(): Request {
        val operationId = node.getTextOrNull("operationId")
        val parameters = buildParameters(schemaRegistry)
        val bodyType = buildBodyType(schemaRegistry)
        val returnType = buildReturnType(schemaRegistry)

        return Request(path, method, operationId, parameters, bodyType, returnType)
    }

    private fun buildParameters(schemaRegistry: SchemaRegistry) =
        node.withArray("parameters").map { it.parseAsRequestParameter(schemaRegistry) }

    private fun buildBodyType(schemaRegistry: SchemaRegistry) =
        // TODO: handle other content types when needed, then again, we don't need them for now
        (node.resolvePath("requestBody/content/application\\/json/schema") as? ObjectNode)
            ?.extractSchemaRef(schemaRegistry)

    private fun buildReturnType(schemaRegistry: SchemaRegistry) =
        // TODO: handle other content types when needed, then again, we don't need them for now
        (node.resolvePath("responses/200/content/application\\/json/schema") as? ObjectNode)
            ?.extractSchemaRef(schemaRegistry)
}

fun JsonNode.parseAsRequest(path: String, method: RequestMethod, schemaRegistry: SchemaRegistry): Request {
    require(this.isObject) { "Json object expected" }

    return RequestBuilder(path, method, this as ObjectNode, schemaRegistry).build()
}