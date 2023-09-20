package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.Request
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.RequestMethod
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseBody
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode

class RequestBuilder(
    private val path: String, private val method: RequestMethod,
    private val node: ObjectNode, private val schemaRegistry: SchemaRegistry
) {

    private val operationId = node.getTextOrNull("operationId") ?: "${method.name} $path"

    fun build(): Request {
        val parameters = buildParameters(schemaRegistry)
        val bodyType = buildBodyType(schemaRegistry)
        val returnType = buildReturnTypes(schemaRegistry)

        return Request(path, method, operationId, parameters, bodyType, returnType)
    }

    private fun buildParameters(schemaRegistry: SchemaRegistry) =
        node.withArray("parameters").map { it.parseAsRequestParameter(schemaRegistry, operationId) }

    // TODO: support other content types too
    private fun buildBodyType(schemaRegistry: SchemaRegistry) =
        (node.resolvePath("requestBody") as? ObjectNode)?.parseAsRequestBody(schemaRegistry, operationId)

    // TODO: support other content types too
    private fun buildReturnTypes(schemaRegistry: SchemaRegistry): List<ResponseBody> {
        val result = mutableListOf<ResponseBody>()
        val responses = node.with("responses")

        responses.fieldNames().forEach {
            val response = responses[it]
            val schemaRef = (response.resolvePath("content/application\\/json/schema") as? ObjectNode)
                ?.extractSchemaRef(schemaRegistry) { "$operationId Response" }
            result.add(ResponseBody(it.toInt(), schemaRef))
        }

        return result
    }
}

fun JsonNode.parseAsRequest(path: String, method: RequestMethod, schemaRegistry: SchemaRegistry): Request {
    require(this.isObject) { "Json object expected" }

    return RequestBuilder(path, method, this as ObjectNode, schemaRegistry).build()
}