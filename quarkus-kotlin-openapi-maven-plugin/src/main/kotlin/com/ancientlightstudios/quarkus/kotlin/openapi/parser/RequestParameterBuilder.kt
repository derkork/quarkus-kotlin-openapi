package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.NameSuggestionHint.nameSuggestion
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ParameterKind
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue
import com.fasterxml.jackson.databind.node.ObjectNode

class RequestParameterBuilder(private val node: ObjectNode) {

    fun ParseContext.build(): TransformableParameter {
        return when (val ref = node.getTextOrNull("\$ref")) {
            null -> extractParameterDefinition()
            else -> extractParameterReference(ref)
        }
    }

    private fun ParseContext.extractParameterDefinition(): TransformableParameter {
        val name = node.getTextOrNull("name")
            ?: SpecIssue("Property 'name' is required for parameter $contextPath")
        val kind = node.getTextOrNull("in")?.let { ParameterKind.fromString(it) }
            ?: SpecIssue("Property 'in' required for parameter $contextPath")

        val required = when (kind) {
            ParameterKind.Path -> true // path parameters are always required
            else -> node.getBooleanOrNull("required") ?: false
        }

        val schema = contextFor("schema").parseAsSchemaUsage()

        return TransformableParameter(name, kind, required, schema)
            .apply {
                originPath = contextPath
            }
    }

    private fun ParseContext.extractParameterReference(ref: String) = rootContext.contextFor(JsonPointer.fromPath(ref))
        .parseAsRequestParameter()
        .apply { nameSuggestion = ref.nameSuggestion() }

}

fun ParseContext.parseAsRequestParameter() =
    contextNode.asObjectNode { "Json object expected for $contextPath" }
        .let {
            RequestParameterBuilder(it).run { this@parseAsRequestParameter.build() }
        }
