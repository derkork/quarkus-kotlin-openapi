package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

class OptimizeSchemeRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        // if there is a single one-item *Of component, convert it into an allOf
        performRefactoring(ReplaceSimpleOfComponentsRefactoring())

        // if there is a single one-item allOf component, replace it
        performRefactoring(ReplaceSingleItemAllOfRefactoring())

        // if there is just a single base-ref with nothing else, replace it
        performRefactoring(ReplaceForwardSchemaRefactoring())
    }

}