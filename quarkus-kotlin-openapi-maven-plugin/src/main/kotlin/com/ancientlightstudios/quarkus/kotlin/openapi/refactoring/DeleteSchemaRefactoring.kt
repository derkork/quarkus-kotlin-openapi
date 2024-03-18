package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchema

class DeleteSchemaRefactoring(private val current: TransformableSchema) : SpecRefactoring {

    override fun RefactoringContext.perform() {
        // TODO: maybe check if there is still something pointing to this schema (see SwapSchemaRefactoring)
        spec.schemas -= current
    }
}