package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema

class DeleteSchemaRefactoring(private val current: OpenApiSchema) : SpecRefactoring {

    override fun RefactoringContext.perform() {
        // TODO: maybe check if there is still something pointing to this schema (see SwapSchemaRefactoring)
        spec.schemas -= current
    }
}