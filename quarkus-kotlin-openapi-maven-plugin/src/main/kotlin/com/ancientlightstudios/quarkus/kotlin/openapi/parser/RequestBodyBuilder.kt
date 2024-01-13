package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableBody
import com.fasterxml.jackson.databind.node.ObjectNode

class RequestBodyBuilder(private val node: ObjectNode) {

    fun ParseContext.build(referencedName: String): TransformableBody {
        return when (val ref = node.getTextOrNull("\$ref")) {
            null -> extractBodyDefinition(referencedName)
            else -> extractBodyReference(ref)
        }
    }

    private fun ParseContext.extractBodyDefinition(referencedName: String) =
        contextFor("content").parseAsBody(node.getBooleanOrNull("required") ?: false, referencedName)

    private fun ParseContext.extractBodyReference(ref: String) = rootContext.contextFor(JsonPointer.fromPath(ref))
        .parseAsRequestBody(ref.substringAfterLast("/"))
}

fun ParseContext.parseAsRequestBody(referencedName: String = "") =
    contextNode.asObjectNode { "Json object expected for ${this.contextPath}" }
        .let {
            RequestBodyBuilder(it).run { this@parseAsRequestBody.build(referencedName) }
        }
