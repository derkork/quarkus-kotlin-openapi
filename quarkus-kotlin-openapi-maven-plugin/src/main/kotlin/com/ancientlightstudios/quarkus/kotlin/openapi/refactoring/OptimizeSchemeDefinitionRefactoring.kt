package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

class OptimizeSchemeDefinitionRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        performRefactoring(ReplaceForwardSchemaDefinitionsRefactoring())
        performRefactoring(ReplaceNullableOverrideRefactoring())

        performRefactoring(RemoveUnusedSchemaDefinitionsRefactoring())
    }

}