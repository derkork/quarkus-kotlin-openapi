package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue
import com.fasterxml.jackson.databind.JsonNode

data class ParseContext(
    val openApiVersion: ApiVersion,
    val contextNode: JsonNode,
    val contextPointer: JsonPointer,
    val schemaDefinitionCollector: SchemaDefinitionCollector,
    val contentTypeMapper: ContentTypeMapper
) {

    // the root context is the first context from which everything else derived
    var rootContext: ParseContext = this
        private set

    val contextPath: String
        get() = contextPointer.path

    fun contextFor(newContextNode: JsonNode, vararg segments: String) = ParseContext(
        openApiVersion,
        newContextNode,
        contextPointer.append(*segments),
        schemaDefinitionCollector,
        contentTypeMapper
    )
        .also { it.rootContext = rootContext }

    fun contextFor(vararg segments: String): ParseContext {
        val newContextNode = contextNode.resolvePointer(JsonPointer.fromSegments(*segments))
            ?: SpecIssue("Path ${this.contextPointer.append(*segments).path} not resolvable.")
        return contextFor(newContextNode, *segments)
    }

    fun contextFor(pointer: JsonPointer): ParseContext {
        val newContextNode = contextNode.resolvePointer(pointer) ?: SpecIssue("Path ${pointer.path} not resolvable.")
        return ParseContext(openApiVersion, newContextNode, pointer, schemaDefinitionCollector, contentTypeMapper)
            .also { it.rootContext = rootContext }
    }

}