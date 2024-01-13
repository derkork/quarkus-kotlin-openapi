package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.setOriginPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableResponse
import com.fasterxml.jackson.databind.node.ObjectNode

class ResponseBuilder(private val code: ResponseCode, private val node: ObjectNode) {

    fun ParseContext.build(referencedName: String): TransformableResponse {
        return when (val ref = node.getTextOrNull("\$ref")) {
            null -> extractResponseDefinition(referencedName)
            else -> extractResponseReference(ref)
        }
    }

    private fun ParseContext.extractResponseDefinition(referencedName: String): TransformableResponse {
        val body = node.get("content")?.let {
            // if a response specifies a body, it is always required
            contextFor(it, "content").parseAsBody(true, referencedName)
        }

        val headers = node.with("headers")
            .propertiesAsList()
            .map { (name, headerNode) -> contextFor(headerNode, "headers", name).parseAsResponseHeader(name) }

        return TransformableResponse(
            code,
            body,
            headers
        ).apply {
            setOriginPath(contextPath)
        }
    }

    private fun ParseContext.extractResponseReference(ref: String) = rootContext.contextFor(JsonPointer.fromPath(ref))
        .parseAsResponse(code, ref.substringAfterLast("/"))

}

fun ParseContext.parseAsResponse(code: ResponseCode, referencedName: String = "") =
    contextNode.asObjectNode { "Json object expected for ${this.contextPath}" }
        .let {
            ResponseBuilder(code, it).run { this@parseAsResponse.build(referencedName) }
        }
