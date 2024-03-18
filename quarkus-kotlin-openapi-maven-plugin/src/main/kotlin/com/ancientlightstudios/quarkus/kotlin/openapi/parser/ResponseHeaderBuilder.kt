package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.NameSuggestionHint.nameSuggestion
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ParameterKind
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableParameter
import com.fasterxml.jackson.databind.node.ObjectNode

class ResponseHeaderBuilder(private val name: String, private val node: ObjectNode) {

    fun ParseContext.build(): TransformableParameter {
        return when (val ref = node.getTextOrNull("\$ref")) {
            null -> extractHeaderDefinition()
            else -> extractHeaderReference(ref)
        }
    }

    private fun ParseContext.extractHeaderDefinition(): TransformableParameter {
        return TransformableParameter(
            name,
            ParameterKind.Header,
            node.getBooleanOrNull("required") ?: false,
            contextFor("schema").parseAsSchema()
        ).apply {
            originPath = contextPath
        }
    }

    private fun ParseContext.extractHeaderReference(ref: String)= rootContext.contextFor(JsonPointer.fromPath(ref))
        .parseAsResponseHeader(name)
        .apply { nameSuggestion = ref.nameSuggestion() }
}

fun ParseContext.parseAsResponseHeader(name: String) =
    contextNode.asObjectNode { "Json object expected for $contextPath" }
        .let {
            ResponseHeaderBuilder(name, it).run { this@parseAsResponseHeader.build() }
        }
