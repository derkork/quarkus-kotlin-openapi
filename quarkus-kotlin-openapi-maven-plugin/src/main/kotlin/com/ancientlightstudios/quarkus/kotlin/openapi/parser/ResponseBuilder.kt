package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.NameSuggestionHint.nameSuggestion
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableResponse
import com.fasterxml.jackson.databind.node.ObjectNode

class ResponseBuilder(private val code: ResponseCode, private val node: ObjectNode) {

    fun ParseContext.build(): TransformableResponse {
        return when (val ref = node.getTextOrNull("\$ref")) {
            null -> extractResponseDefinition()
            else -> extractResponseReference(ref)
        }
    }

    private fun ParseContext.extractResponseDefinition(): TransformableResponse {
        val body = node.get("content")?.let {
            // if a response specifies a body, it is always required
            TransformableBody(true, contextFor(it, "content").parseAsContent())
        }

        val headers = node.with("headers")
            .propertiesAsList()
            .map { (name, headerNode) -> contextFor(headerNode, "headers", name).parseAsResponseHeader(name) }

        return TransformableResponse(code, body, headers).apply {
            originPath = contextPath
        }
    }

    private fun ParseContext.extractResponseReference(ref: String) = rootContext.contextFor(JsonPointer.fromPath(ref))
        .parseAsResponse(code)
        .apply { nameSuggestion = ref.nameSuggestion() }

}

fun ParseContext.parseAsResponse(code: ResponseCode) =
    contextNode.asObjectNode { "Json object expected for $contextPath" }
        .let {
            ResponseBuilder(code, it).run { this@parseAsResponse.build() }
        }
