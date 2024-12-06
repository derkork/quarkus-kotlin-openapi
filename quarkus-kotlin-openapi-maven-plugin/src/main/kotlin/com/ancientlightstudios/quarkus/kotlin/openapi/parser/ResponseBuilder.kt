package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.NameSuggestionHint.nameSuggestion
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiResponse
import com.fasterxml.jackson.databind.node.ObjectNode

class ResponseBuilder(private val code: ResponseCode, private val node: ObjectNode) {

    fun ParseContext.build(): OpenApiResponse {
        return when (val ref = node.getTextOrNull("\$ref")) {
            null -> extractResponseDefinition()
            else -> extractResponseReference(ref)
        }
    }

    private fun ParseContext.extractResponseDefinition(): OpenApiResponse {
        val body = node.get("content")?.let {
            // if a response specifies a body, it is always required
            OpenApiBody(true, contextFor(it, "content").parseAsContent())
        }

        val headers = node.with("headers")
            .propertiesAsList()
            .map { (name, headerNode) -> contextFor(headerNode, "headers", name).parseAsResponseHeader(name) }

        val interfaceName = node.getTextOrNull("x-generic-response-name")

        return OpenApiResponse(code, body, headers, interfaceName?.trim()).apply {
            originPath = contextPath
        }
    }

    private fun ParseContext.extractResponseReference(ref: String) = rootContext.contextFor(JsonPointer.fromPath(ref))
        .parseAsResponse(code)
        .apply { nameSuggestion = ref.referencedComponentName() }

}

fun ParseContext.parseAsResponse(code: ResponseCode) =
    contextNode.asObjectNode { "Json object expected for $contextPath" }
        .let {
            ResponseBuilder(code, it).run { this@parseAsResponse.build() }
        }
