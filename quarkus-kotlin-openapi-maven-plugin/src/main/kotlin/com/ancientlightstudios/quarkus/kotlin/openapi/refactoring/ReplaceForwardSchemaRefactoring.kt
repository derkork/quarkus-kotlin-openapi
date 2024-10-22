package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.BaseSchemaComponent.Companion.baseSchemaComponent

// if a schema only contains a BaseSchemaComponent, we can replace it with the inner schema
class ReplaceForwardSchemaRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        spec.inspect {
            schemas {
                // if there is more than one component, we can't handle it here
                if (schema.components.size != 1) {
                    return@schemas
                }

                val base = schema.baseSchemaComponent() ?: return@schemas
                performRefactoring(SwapSchemaRefactoring(schema, base.schema))
                performRefactoring(DeleteSchemaRefactoring(schema))
            }
        }
    }

}