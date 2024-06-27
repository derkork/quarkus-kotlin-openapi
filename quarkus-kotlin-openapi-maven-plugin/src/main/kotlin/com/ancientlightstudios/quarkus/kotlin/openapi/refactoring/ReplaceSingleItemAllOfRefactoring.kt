package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.AllOfComponent

class ReplaceSingleItemAllOfRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        spec.inspect {
            schemas {
                // if there is more than one component, we can't handle it here
                if (schema.components.size != 1) {
                    return@schemas
                }

                val allOf = schema.getComponent<AllOfComponent>() ?: return@schemas
                // can't replace it, without changing the meaning
                if (allOf.schemas.size > 1) {
                    return@schemas
                }

                performRefactoring(SwapSchemaRefactoring(schema, allOf.schemas.first().schema))
                performRefactoring(DeleteSchemaRefactoring(schema))
            }
        }
    }

}
