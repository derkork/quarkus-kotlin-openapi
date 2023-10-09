package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiVersion
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseHeader
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseHeaderDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseHeaderReference
import com.fasterxml.jackson.databind.node.ObjectNode

class ResponseHeaderBuilder(private val node: ObjectNode) {

    fun ParseContext.build(): ResponseHeader {
        return when (val ref = node.getTextOrNull("\$ref")) {
            null -> extractHeaderDefinition()
            else -> extractHeaderReference(ref)
        }
    }

    private fun ParseContext.extractHeaderDefinition(): ResponseHeader {
        val schema = contextFor("schema").parseAsSchema()

        return ResponseHeaderDefinition(
            schema, node.getTextOrNull("description"),
            node.getBooleanOrNull("deprecated") ?: false,
            node.getBooleanOrNull("required") ?: false
        )
    }

    private fun ParseContext.extractHeaderReference(ref: String): ResponseHeader {
        val (targetName, responseHeader) = referenceResolver.resolveResponseHeader(ref)
        val description = when (openApiVersion) {
            // not supported in v3.0
            OpenApiVersion.V3_0 -> null
            OpenApiVersion.V3_1 -> node.getTextOrNull("description")
        }

        return ResponseHeaderReference(targetName, responseHeader, description)
    }
}

fun ParseContext.parseAsResponseHeader() =
    contextNode.asObjectNode { "Json object expected for ${this.contextPath}" }
        .let {
            ResponseHeaderBuilder(it).run { this@parseAsResponseHeader.build() }
        }
