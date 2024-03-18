package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.pop

class SchemaCollector {

    private val collectedReferences = mutableMapOf<String, TransformableSchema>()
    private val unresolvedSchema = mutableSetOf<TransformableSchema>()

    fun registerSchema(contextPath: String) = collectedReferences.getOrPut(contextPath) {
        TransformableSchema("")
            .apply {
                originPath = contextPath
            }.also {
                unresolvedSchema.add(it)
            }
    }

    val nextUnresolvedSchema: TransformableSchema?
        get() = unresolvedSchema.pop()

}