package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiContentMapping
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue
import com.fasterxml.jackson.databind.node.ObjectNode

class ContentBuilder(private val node: ObjectNode) {

    fun ParseContext.build(): OpenApiContentMapping {
        val contents = node.propertiesAsList()
            .map { (rawContentType, _) ->
                val contentType = contentTypeMapper.mapContentType(rawContentType)
                val schema = contextFor(rawContentType, "schema").parseAsSchema()
                OpenApiContentMapping(contentType, rawContentType, schema).apply {
                    originPath = contextPath
                }
            }

        when (contents.size) {
            0 -> SpecIssue("At least one content type for a body required. Found in $contextPath")
            1 -> return contents.first()
            else -> SpecIssue("More than one content type for a body is not yet supported. Found in $contextPath")
        }
    }

}

fun ParseContext.parseAsContent() =
    contextNode.asObjectNode { "Json object expected for $contextPath" }
        .let {
            ContentBuilder(it).run { this@parseAsContent.build() }
        }
