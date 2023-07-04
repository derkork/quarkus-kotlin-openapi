package com.tallence.quarkus.kotlin.openapi.builder

import com.tallence.quarkus.kotlin.openapi.Request
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.tallence.quarkus.kotlin.openapi.getTextOrNull

class RequestBuilder(
    private val path: String, private val method: String,
    private val node: ObjectNode, private val schemaRegistry: SchemaRegistry
) {

    fun build(): Request {
        val operationId = node.getTextOrNull("operationId")
        val parameters = buildParameters(schemaRegistry)

        return Request(path, method, operationId, parameters)
    }

    private fun buildParameters(schemaRegistry: SchemaRegistry) =
        node.withArray("parameters").map { it.parseAsRequestParameter(schemaRegistry) }
}

fun JsonNode.parseAsRequest(path: String, method: String, schemaRegistry: SchemaRegistry): Request {
    if (!this.isObject) {
        throw IllegalArgumentException("Json object expected")
    }

    return RequestBuilder(path, method, this as ObjectNode, schemaRegistry).build()
}