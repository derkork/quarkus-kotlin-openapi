package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.pop

class SchemaCollector {

    private val collectedReferences = mutableMapOf<String, OpenApiSchema>()
    private val unresolvedSchema = mutableSetOf<OpenApiSchema>()

    fun registerSchema(contextPath: String) = collectedReferences.getOrPut(contextPath) {
        OpenApiSchema("")
            .apply {
                originPath = contextPath
            }.also {
                unresolvedSchema.add(it)
            }
    }

    val nextUnresolvedSchema: OpenApiSchema?
        get() = unresolvedSchema.pop()

}