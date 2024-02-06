package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaUsage

class SchemaUsageBuilder {

    fun ParseContext.build() =
        TransformableSchemaUsage(schemaDefinitionCollector.registerSchemaDefinition(contextPath))
            .apply {
                originPath = contextPath
            }

}

fun ParseContext.parseAsSchemaUsage() =
    contextNode.asObjectNode { "Json object expected for $contextPath" }
        .let {
            SchemaUsageBuilder().run { this@parseAsSchemaUsage.build() }
        }
