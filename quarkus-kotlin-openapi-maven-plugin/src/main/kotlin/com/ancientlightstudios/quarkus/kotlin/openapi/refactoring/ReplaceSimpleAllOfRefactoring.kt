package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.AllOfComponent.Companion.allOfComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.SomeOfComponent

class ReplaceSimpleAllOfRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        do {
            val schemas = spec.schemas.filter { owner ->
                val allOf = owner.allOfComponent() ?: return@filter false
                // none allOf-item must contain other *of components
                allOf.schemas.all { it.schema.components.filterIsInstance<SomeOfComponent>().isEmpty() }
            }
            if (schemas.isEmpty()) {
                return
            }

            schemas.forEach { owner ->
                val allOf = owner.allOfComponent() ?: return@forEach
                allOf.schemas.forEach { donator ->
                    owner.components += donator.schema.components
                }
                owner.components -= allOf
            }
        } while (true)
    }

}
