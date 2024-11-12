package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.NameSuggestionHint.nameSuggestion
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiBody
import com.fasterxml.jackson.databind.node.ObjectNode

class RequestBodyBuilder(private val node: ObjectNode) {

    fun ParseContext.build(): OpenApiBody {
        return when (val ref = node.getTextOrNull("\$ref")) {
            null -> extractBodyDefinition()
            else -> extractBodyReference(ref)
        }
    }

    private fun ParseContext.extractBodyDefinition() = OpenApiBody(
        node.getBooleanOrNull("required") ?: false, contextFor("content").parseAsContent()
    )

    private fun ParseContext.extractBodyReference(ref: String) = rootContext.contextFor(JsonPointer.fromPath(ref))
        .parseAsRequestBody()
        .apply { nameSuggestion = ref.nameSuggestion() }
}

fun ParseContext.parseAsRequestBody() =
    contextNode.asObjectNode { "Json object expected for $contextPath" }
        .let {
            RequestBodyBuilder(it).run { this@parseAsRequestBody.build() }
        }
