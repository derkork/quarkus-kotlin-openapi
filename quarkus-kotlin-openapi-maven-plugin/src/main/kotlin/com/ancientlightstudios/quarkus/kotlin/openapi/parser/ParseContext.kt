package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiVersion
import com.fasterxml.jackson.databind.JsonNode

data class ParseContext(
    val openApiVersion: OpenApiVersion,
    val contextNode: JsonNode,
    val contextPath: String,
    val referenceResolver: ReferenceResolver
) {

    fun contextFor(newContextNode: JsonNode, path: String) =
        ParseContext(openApiVersion, newContextNode, "${this.contextPath}/$path", referenceResolver)

    fun contextFor(path: String): ParseContext {
        val newContextNode = contextNode.resolvePath(path)
            ?: throw IllegalStateException("Path ${this.contextPath}/$path not resolvable.")
        return contextFor(newContextNode, path)
    }

}