package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.pop

class SchemaDefinitionCollector {

    private val collectedReferences = mutableMapOf<String, TransformableSchemaDefinition>()
    private val unresolvedSchemaDefinition = mutableSetOf<TransformableSchemaDefinition>()

    fun registerSchemaDefinition(contextPath: String) = collectedReferences.getOrPut(contextPath) {
        TransformableSchemaDefinition("")
            .apply {
                originPath = contextPath
            }.also {
                unresolvedSchemaDefinition.add(it)
            }
    }

    val nextUnresolvedSchemaDefinition: TransformableSchemaDefinition?
        get() = unresolvedSchemaDefinition.pop()

}