package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.AllOfComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.SomeOfComponent

// checks for schemas with a single one-item *Of component replaces it with an allOf component
class ReplaceSimpleOfComponentsRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        spec.inspect {
            schemaDefinitions {
                // is there a single *Of component?
                val someOfComponents = schemaDefinition.components.filterIsInstance<SomeOfComponent>()
                val someOfComponent = when (someOfComponents.size) {
                    1 -> someOfComponents.first()
                    else -> return@schemaDefinitions
                }

                // is it already a allOf component?
                if (someOfComponent is AllOfComponent) {
                    return@schemaDefinitions
                }

                // can't replace it, without changing the meaning
                if (someOfComponent.schemas.size > 1) {
                    return@schemaDefinitions
                }

                val newComponents = schemaDefinition.components.toMutableList()
                newComponents.remove(someOfComponent)
                newComponents.add(AllOfComponent(someOfComponent.schemas))
                schemaDefinition.components = newComponents
            }
        }
    }

}
