package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.hasTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class AssignTypesToSchemasRefactoring(private val typeMapper: TypeMapper) : SpecRefactoring {

    override fun RefactoringContext.perform() {
        // this can be used by the nested type creators to finalize the type usages once all types are created
        val typeResolver = TypeResolver()

        // just take everything as the starting set
        var tasks = spec.schemas.toMutableSet()

        // while we have work left
        while (tasks.isNotEmpty()) {
            val sizeBefore = tasks.size

            // handle all definitions without a base definition or *Of component
            performRefactoring(AssignTypesToSimpleSchemasRefactoring(tasks, typeMapper, typeResolver))
            // handle all definitions with just a base definition
            performRefactoring(AssignTypesToSimpleExtendedSchemasRefactoring(tasks, typeMapper, typeResolver))

            performRefactoring(AssignTypesToSomeOfSchemasRefactoring(tasks, typeResolver))

            // everything which was not yet mapped, for the next loop
            tasks = spec.schemas.filterNot { it.hasTypeDefinition }.toMutableSet()
            if (sizeBefore <= tasks.size) {
                ProbableBug("endless loop detected while converting schemas into types")
            }
        }

        // now all types are available, and we can initialize the remaining usages
        typeResolver.resolve()
    }

}

class TypeResolver {

    private val lazyTypes = mutableListOf<Pair<TypeUsage, () -> TypeDefinition>>()
    private val typeMappings = mutableListOf<Pair<TypeUsage, TypeUsage>>()

    fun schedule(target: TypeUsage, source: () -> TypeDefinition) {
        lazyTypes.add(target to source)
    }

    fun schedule(target: TypeUsage, source: TypeUsage) {
        typeMappings.add(target to source)
    }

    fun resolve() {
        lazyTypes.forEach {
            it.first.type = it.second()
        }

        typeMappings.forEach {
            it.first.type = it.second.type
        }
    }

}