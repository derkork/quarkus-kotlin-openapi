package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.hasTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class AssignTypesRefactoring(private val typeMapper: TypeMapper) : SpecRefactoring {

    override fun RefactoringContext.perform() {
        // just take everything as the starting set
        var tasks = spec.schemaDefinitions.toMutableSet()

        // while we have work left
        while (tasks.isNotEmpty()) {
            val sizeBefore = tasks.size
            // handle all definitions without a base definition or *Of component
            performRefactoring(AssignTypesToSimpleDefinitionsRefactoring(tasks, typeMapper))
            // handle all definitions with just a base definition
            performRefactoring(AssignTypesToSimpleExtendedDefinitionsRefactoring(tasks, typeMapper))


            // everything which was not yet mapped, for the next loop
            tasks = spec.schemaDefinitions.filterNot { it.hasTypeDefinition }.toMutableSet()
            if (sizeBefore <= tasks.size) {
                ProbableBug("endless loop detected while converting schemas into types")
            }
        }
    }

}