package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.BaseSchemaComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.OneOfComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.OneOfComponent.Companion.oneOfComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeUsage

// converts schema without any referencing components but a *of component
class AssignTypesToSomeOfSchemasRefactoring(
    private val tasks: MutableSet<TransformableSchema>,
    private val typeResolver: TypeResolver
) : SpecRefactoring {

    override fun RefactoringContext.perform() {
        val candidates = tasks
            .filter { schema -> schema.components.none { it is BaseSchemaComponent } }
            // TODO: replace with SomeOf 
            .filter { schema -> schema.components.any { it is OneOfComponent } }
            .toSet()

        // remove them from the tasks list, because nobody has to handle them anymore
        tasks.removeAll(candidates)

        candidates.forEach { definition ->
            val oneOf = definition.oneOfComponent()
            performRefactoring(CreateSimpleOneOfTypeRefactoring(definition, oneOf!!, typeResolver))
        }
    }

}
