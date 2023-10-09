package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiVersion
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.RequestBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseHeader
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.parameter.Parameter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.Schema
import com.fasterxml.jackson.databind.JsonNode

class ReferenceResolver(private val openApiVersion: OpenApiVersion, private val root: JsonNode) {

    private val schemas = mutableMapOf<String, Pair<String, Schema>>()
    private val parameters = mutableMapOf<String, Pair<String, Parameter>>()
    private val requestBodies = mutableMapOf<String, Pair<String, RequestBody>>()
    private val responseBodies = mutableMapOf<String, Pair<String, ResponseBody>>()
    private val headers = mutableMapOf<String, Pair<String, ResponseHeader>>()

    private fun resolveReference(reference: String, message: () -> String): ParseContext {
        val node = root.resolvePath(reference) ?: throw IllegalArgumentException(message())
        return ParseContext(openApiVersion, node, reference, this)
    }

    private fun String.targetName() = substringAfterLast('/')

    fun resolveSchema(reference: String) = schemas.getOrPut(reference) {
        resolveReference(reference) { "Unresolvable schema reference $reference" }
            .parseAsSchema()
            .let { reference.targetName() to it }
    }

    fun resolveParameter(reference: String) = parameters.getOrPut(reference) {
        resolveReference(reference) { "Unresolvable parameter reference $reference" }
            .parseAsRequestParameter()
            .let { reference.targetName() to it }
    }

    fun resolveRequestBody(reference: String) = requestBodies.getOrPut(reference) {
        resolveReference(reference) { "Unresolvable request body reference $reference" }
            .parseAsRequestBody()
            .let { reference.targetName() to it }
    }

    fun resolveResponseBody(reference: String) = responseBodies.getOrPut(reference) {
        resolveReference(reference) { "Unresolvable response body reference $reference" }
            .parseAsResponseBody()
            .let { reference.targetName() to it }
    }

    fun resolveResponseHeader(reference: String) = headers.getOrPut(reference) {
        resolveReference(reference) { "Unresolvable response header reference $reference" }
            .parseAsResponseHeader()
            .let { reference.targetName() to it }
    }

}