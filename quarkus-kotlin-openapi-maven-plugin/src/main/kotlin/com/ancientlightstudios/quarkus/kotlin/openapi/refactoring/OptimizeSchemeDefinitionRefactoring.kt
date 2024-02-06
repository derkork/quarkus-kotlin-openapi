package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

class OptimizeSchemeDefinitionRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        performRefactoring(ReplaceForwardSchemaDefinitionsRefactoring())
        // apply forcenullabel to schemas usage
    }
}