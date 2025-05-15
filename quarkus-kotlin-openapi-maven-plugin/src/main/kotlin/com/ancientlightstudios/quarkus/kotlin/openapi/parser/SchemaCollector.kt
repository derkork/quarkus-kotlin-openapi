package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.NameSuggestionHint.nameSuggestion
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.pop

class SchemaCollector {

    private val collectedReferences = mutableMapOf<String, OpenApiSchema>()
    private val unresolvedSchema = mutableSetOf<OpenApiSchema>()

    fun registerSchema(contextPath: String) = collectedReferences.getOrPut(contextPath) {
        // name is empty by default and will be set during refactoring
        // when all information are available
        OpenApiSchema()
            .apply {
                originPath = contextPath
                nameSuggestion = contextPath.referencedComponentName()
            }.also {
                unresolvedSchema.add(it)
            }
    }

    val nextUnresolvedSchema: OpenApiSchema?
        get() = unresolvedSchema.pop()

}