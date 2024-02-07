package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

class AssignTypesRefactoring(private val typeMapper: TypeMapper) : SpecRefactoring {

    override fun RefactoringContext.perform() {
        performRefactoring(SchemaDefinitionToTypeDefinitionRefactoring(typeMapper))
        performRefactoring(SchemaUsageToTypeUsageRefactoring())
    }

}