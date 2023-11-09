package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiVersion
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseBodyDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseBodyReference
import com.fasterxml.jackson.databind.node.ObjectNode

class ResponseBodyBuilder(private val node: ObjectNode) {

    fun ParseContext.build(): ResponseBody {
        return when (val ref = node.getTextOrNull("\$ref")) {
            null -> extractBodyDefinition()
            else -> extractBodyReference(ref)
        }
    }

    private fun ParseContext.extractBodyDefinition(): ResponseBody {
        val schema = contextNode.resolvePointer("content/application\\/json/schema")?.let {
            contextFor(it, "content/application\\/json/schema").parseAsSchema()
        }

        val headers = node.with("headers")
            .propertiesAsList()
            .map { (name, headerNode) ->
                val header = contextFor(headerNode, "headers/$name").parseAsResponseHeader()
                name to header
            }

        return ResponseBodyDefinition(schema, node.getTextOrNull("description"), headers)
    }

    private fun ParseContext.extractBodyReference(ref: String): ResponseBody {
        val (targetName, responseBody) = referenceResolver.resolveResponseBody(ref)
        val description = when (openApiVersion) {
            // not supported in v3.0
            OpenApiVersion.V3_0 -> null
            OpenApiVersion.V3_1 -> node.getTextOrNull("description")
        }

        return ResponseBodyReference(targetName, responseBody, description)
    }
}

fun ParseContext.parseAsResponseBody() =
    contextNode.asObjectNode { "Json object expected for ${this.contextPath}" }
        .let {
            ResponseBodyBuilder(it).run { this@parseAsResponseBody.build() }
        }
