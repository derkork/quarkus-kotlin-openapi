package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.setOriginPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableBody
import com.fasterxml.jackson.databind.node.ObjectNode

class ResponseBodyBuilder(private val required: Boolean, private val node: ObjectNode) {

    fun ParseContext.build(referencedName: String): TransformableBody {
        val contentTypes = node.propertiesAsList()
            .map { it.first }

        return TransformableBody(required, contentTypes).apply {
            setOriginPath(contextPath)
        }

        // TODO: schemas
        // val schema = contextFor("content", "application/json", "schema").parseAsSchema(referencedName)
    }

}

fun ParseContext.parseAsBody(required: Boolean, referencedName: String = "") =
    contextNode.asObjectNode { "Json object expected for ${this.contextPath}" }
        .let {
            ResponseBodyBuilder(required, it).run { this@parseAsBody.build(referencedName) }
        }
