package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentMapping
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableBody
import com.fasterxml.jackson.databind.node.ObjectNode

class ResponseBodyBuilder(private val required: Boolean, private val node: ObjectNode) {

    fun ParseContext.build(): TransformableBody {
        val content = node.propertiesAsList()
            .map { (rawContentType, _) ->
                val contentType = contentTypeMapper.mapContentType(rawContentType)
                ContentMapping(contentType, rawContentType, contextFor(rawContentType, "schema").parseAsSchemaUsage())
            }

        return TransformableBody(required, content).apply {
            originPath = contextPath
        }
    }

}

fun ParseContext.parseAsBody(required: Boolean) =
    contextNode.asObjectNode { "Json object expected for $contextPath" }
        .let {
            ResponseBodyBuilder(required, it).run { this@parseAsBody.build() }
        }
