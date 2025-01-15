package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSpec

class RefactoringContext(val spec: OpenApiSpec, val config: Config) {

    fun performRefactoring(refactoring: SpecRefactoring) {
        refactoring.apply { perform() }
    }

}