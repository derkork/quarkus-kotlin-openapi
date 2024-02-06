package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaProperty
import com.fasterxml.jackson.databind.node.ObjectNode

class SchemaPropertyBuilder(private val name: String, private val node: ObjectNode) {

    fun ParseContext.build(): TransformableSchemaProperty {
        return TransformableSchemaProperty(name, parseAsSchemaUsage())
            .apply {
                originPath = contextPath
            }
    }

}

fun ParseContext.parseAsSchemaProperty(name: String) =
    contextNode.asObjectNode { "Json object expected for $contextPath" }
        .let {
            SchemaPropertyBuilder(name, it).run { this@parseAsSchemaProperty.build() }
        }
