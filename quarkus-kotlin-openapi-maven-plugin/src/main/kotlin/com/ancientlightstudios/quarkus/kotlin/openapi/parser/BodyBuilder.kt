package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableBody
import com.fasterxml.jackson.databind.node.ObjectNode

class ResponseBodyBuilder(private val required: Boolean, private val node: ObjectNode) {

    fun ParseContext.build(referencedName: String): TransformableBody {
        val contentTypes = node.propertiesAsList()
            .map { ContentType.fromString(it.first) }

        // TODO: verify that there is at least one content-type set

        return TransformableBody(required, contentTypes).apply {
            originPath = contextPath
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
