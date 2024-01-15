package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableHeaderParameter
import com.fasterxml.jackson.databind.node.ObjectNode

class ResponseHeaderBuilder(private val name: String, private val node: ObjectNode) {

    fun ParseContext.build(referencedName: String): TransformableHeaderParameter {
        return when (val ref = node.getTextOrNull("\$ref")) {
            null -> extractHeaderDefinition(referencedName)
            else -> extractHeaderReference(ref)
        }
    }

    private fun ParseContext.extractHeaderDefinition(referencedName: String): TransformableHeaderParameter {
        return TransformableHeaderParameter(
            name,
            node.getBooleanOrNull("required") ?: false
        ).apply {
            originPath = contextPath
        }

        // TODO: schemas
        // val schema = contextFor("content", "application/json", "schema").parseAsSchema(referencedName)
    }

    private fun ParseContext.extractHeaderReference(ref: String) = rootContext.contextFor(JsonPointer.fromPath(ref))
        .parseAsResponseHeader(name, ref.substringAfterLast("/"))
}

fun ParseContext.parseAsResponseHeader(name: String, referencedName: String = "") =
    contextNode.asObjectNode { "Json object expected for ${this.contextPath}" }
        .let {
            ResponseHeaderBuilder(name, it).run { this@parseAsResponseHeader.build(referencedName) }
        }
