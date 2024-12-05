package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.NameSuggestionHint.nameSuggestion
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ParameterKind
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiParameter
import com.fasterxml.jackson.databind.node.ObjectNode

class ResponseHeaderBuilder(private val name: String, private val node: ObjectNode) {

    fun ParseContext.build(): OpenApiParameter {
        return when (val ref = node.getTextOrNull("\$ref")) {
            null -> extractHeaderDefinition()
            else -> extractHeaderReference(ref)
        }
    }

    private fun ParseContext.extractHeaderDefinition(): OpenApiParameter {
        return OpenApiParameter(
            name,
            ParameterKind.Header,
            node.getBooleanOrNull("required") ?: false,
            contextFor("content").parseAsContent()
        ).apply {
            originPath = contextPath
        }
    }

    private fun ParseContext.extractHeaderReference(ref: String)= rootContext.contextFor(JsonPointer.fromPath(ref))
        .parseAsResponseHeader(name)
        .apply { nameSuggestion = ref.referencedComponentName() }
}

fun ParseContext.parseAsResponseHeader(name: String) =
    contextNode.asObjectNode { "Json object expected for $contextPath" }
        .let {
            ResponseHeaderBuilder(name, it).run { this@parseAsResponseHeader.build() }
        }
