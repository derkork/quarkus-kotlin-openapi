package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.BaseDefinitionComponent

// if a schema definition only contains a BaseDefinitionComponent, we can replace it with the inner schema
class ReplaceForwardSchemaDefinitionsRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        spec.inspect {
            schemaDefinitions {
                // if there is more than one component, we can't handle it here
                if (schemaDefinition.components.size != 1) {
                    return@schemaDefinitions
                }

                val base = schemaDefinition.getComponent<BaseDefinitionComponent>() ?: return@schemaDefinitions

                val target = base.innerSchema.schemaDefinition
                performRefactoring(SwapSchemaDefinitionRefactoring(schemaDefinition, target))
                performRefactoring(DeleteSchemaDefinitionRefactoring(schemaDefinition))
            }
        }
    }

}