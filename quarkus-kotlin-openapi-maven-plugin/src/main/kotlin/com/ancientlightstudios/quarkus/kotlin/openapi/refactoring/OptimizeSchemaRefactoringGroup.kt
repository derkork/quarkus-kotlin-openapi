package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

class OptimizeSchemaRefactoringGroup : SpecRefactoring {

    override fun RefactoringContext.perform() {
        // replace a single one-item *Of component with an allOf component
        // (doesn't change the meaning but makes life easier in the next steps)
        performRefactoring(ReplaceSimpleOfComponentsRefactoring())

        // TODO: handle situations with nested allOf, oneOf components. (like boolean combination where stuff
        // from the allOf is merged into the oneOfs etc to get the same meaning)

        // replace allOf components with the components of the nested schemas
        performRefactoring(ReplaceAllOfComponentsRefactoring())
        // after this point, there shouldn't be a single schema with an allOf component

        // replaces schemas with just a single base schema component with the referenced schema
        performRefactoring(ReplaceForwardSchemaRefactoring())

        // flatten schemas by copying components from their base schemas. relations are still available via hint
        performRefactoring(InlineSchemaComponentsRefactoring())
        // after this point there shouldn't be a single schema with a base schema component

        // find out if a schema is a final model or just an overlay
        performRefactoring(AssignTypeToSchemaRefactoring())

        // find out in which direction a schema is used
        performRefactoring(AssignSchemaDirectionRefactoring())

        // finds bidirectional object models with read-only/write-only properties and adds the split flag hint to them.
        // recursively searches for schemas which use other schemas with the split flag and checks if they also have
        // to be marked with the flag
        performRefactoring(AssignSplitFlagToSchemaRefactoring())
    }

}