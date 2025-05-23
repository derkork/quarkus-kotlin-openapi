package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.AllOfComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.SomeOfComponent

// checks for schemas with a single one-item *Of component replaces it with an allOf component
class ReplaceSimpleOfComponentsRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        spec.inspect {
            schemas {
                // is there a single *Of component?
                val someOfComponents = schema.components.filterIsInstance<SomeOfComponent>()
                val someOfComponent = when (someOfComponents.size) {
                    1 -> someOfComponents.first()
                    else -> return@schemas
                }

                // is it already a allOf component?
                if (someOfComponent is AllOfComponent) {
                    return@schemas
                }

                // can't replace it, without changing the meaning
                if (someOfComponent.options.size > 1) {
                    return@schemas
                }

                // remove old component and add the new one instead
                schema.components -= someOfComponent
                schema.components += AllOfComponent(someOfComponent.options)
            }
        }
    }

}
