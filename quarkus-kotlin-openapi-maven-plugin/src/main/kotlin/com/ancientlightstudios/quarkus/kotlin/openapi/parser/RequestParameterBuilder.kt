package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue
import com.fasterxml.jackson.databind.node.ObjectNode

class RequestParameterBuilder(private val node: ObjectNode) {

    fun ParseContext.build(referencedName: String): TransformableParameter {
        return when (val ref = node.getTextOrNull("\$ref")) {
            null -> extractParameterDefinition(referencedName)
            else -> extractParameterReference(ref)
        }
    }

    private fun ParseContext.extractParameterDefinition(referencedName: String): TransformableParameter {
        val name = node.getTextOrNull("name")
            ?: SpecIssue("Property 'name' is required for parameter $contextPath")
        val kind = node.getTextOrNull("in")
            ?: SpecIssue("Property 'in' required for parameter $contextPath")

        // val schema = contextFor("schema").parseAsSchema(referencedName)

        return when (kind) {
            "path" -> extractPathParameterDefinition(name)
            "query" -> extractQueryParameterDefinition(name)
            "header" -> extractHeaderParameterDefinition(name)
            "cookie" -> extractCookieParameterDefinition(name)
            else -> SpecIssue("Unknown type '$kind' for parameter $contextPath")
        }.apply {
            originPath = contextPath
        }
    }

    private fun ParseContext.extractParameterReference(ref: String) = rootContext.contextFor(JsonPointer.fromPath(ref))
        .parseAsRequestParameter(ref.substringAfterLast("/"))

    private fun ParseContext.extractPathParameterDefinition(name: String) = TransformablePathParameter(name)

    private fun ParseContext.extractQueryParameterDefinition(name: String) =
        TransformableQueryParameter(
            name,
            node.getBooleanOrNull("required") ?: false
        )

    private fun ParseContext.extractHeaderParameterDefinition(name: String) =
        TransformableHeaderParameter(
            name,
            node.getBooleanOrNull("required") ?: false
        )

    private fun ParseContext.extractCookieParameterDefinition(name: String) =
        TransformableCookieParameter(
            name,
            node.getBooleanOrNull("required") ?: false
        )

}

fun ParseContext.parseAsRequestParameter(referencedName: String = "") =
    contextNode.asObjectNode { "Json object expected for $contextPath" }
        .let {
            RequestParameterBuilder(it).run { this@parseAsRequestParameter.build(referencedName) }
        }
