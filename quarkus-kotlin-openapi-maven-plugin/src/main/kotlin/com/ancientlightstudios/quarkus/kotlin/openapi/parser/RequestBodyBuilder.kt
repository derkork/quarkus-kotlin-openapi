package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiVersion
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.RequestBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.RequestBodyDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.RequestBodyReference
import com.fasterxml.jackson.databind.node.ObjectNode

class RequestBodyBuilder(private val node: ObjectNode) {

    fun ParseContext.build(): RequestBody {
        return when (val ref = node.getTextOrNull("\$ref")) {
            null -> extractBodyDefinition()
            else -> extractBodyReference(ref)
        }
    }

    private fun ParseContext.extractBodyDefinition(): RequestBody {
        val schema = contextFor("content/application\\/json/schema").parseAsSchema()

        return RequestBodyDefinition(
            schema, node.getTextOrNull("description"),
            node.getBooleanOrNull("required") ?: false
        )
    }

    private fun ParseContext.extractBodyReference(ref: String): RequestBody {
        val (targetName, requestBody) = referenceResolver.resolveRequestBody(ref)
        val description = when (openApiVersion) {
            // not supported in v3.0
            OpenApiVersion.V3_0 -> null
            OpenApiVersion.V3_1 -> node.getTextOrNull("description")
        }

        return RequestBodyReference(targetName, requestBody, description)
    }
}

fun ParseContext.parseAsRequestBody() =
    contextNode.asObjectNode { "Json object expected for ${this.contextPath}" }
        .let {
            RequestBodyBuilder(it).run { this@parseAsRequestBody.build() }
        }
