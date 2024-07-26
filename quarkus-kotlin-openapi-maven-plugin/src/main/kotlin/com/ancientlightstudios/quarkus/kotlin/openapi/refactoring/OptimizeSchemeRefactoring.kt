package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

class OptimizeSchemeRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        // if there is a single one-item *Of component, convert it into an allOf
        performRefactoring(ReplaceSimpleOfComponentsRefactoring())

        // TODO
        // - allOf mit einem oneOf - alles in die oneOf branches mergen und entfernt sich selbst
        // - allOf mit mehreren oneOfs - merged alles in die oneOf branches und macht oneOf mit kreuzprodukt und entfernt sich selbst

        // replace all simple allOf schemas (schemas without other *of components)
        performRefactoring(ReplaceSimpleAllOfRefactoring())

        // if there is just a single base-ref with nothing else, replace it
        performRefactoring(ReplaceForwardSchemaRefactoring())
    }

}