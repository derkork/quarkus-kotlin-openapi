package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.ToBeChecked
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.BaseSchemaComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.OneOfComponent

@ToBeChecked
// converts schema without any referencing components but a *of component
class AssignTypesToSomeOfSchemasRefactoring(
    private val tasks: MutableSet<OpenApiSchema>,
    private val typeResolver: TypeResolver
) : SpecRefactoring {

    override fun RefactoringContext.perform() {
//        val candidates = tasks
//            .filter { schema -> schema.components.none { it is BaseSchemaComponent } }
//            // TODO: replace with SomeOf
//            .filter { schema -> schema.components.any { it is OneOfComponent } }
//            .toSet()
//
//        // remove them from the tasks list, because nobody has to handle them anymore
//        tasks.removeAll(candidates)
//
//        candidates.forEach { definition ->
//            val oneOf = definition.oneOfComponent()
//            performRefactoring(CreateSimpleOneOfTypeRefactoring(definition, oneOf!!, typeResolver))
//        }
    }

}
