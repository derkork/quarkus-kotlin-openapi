package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.BaseDefinitionComponent

class ReplaceForwardSchemaDefinitionsRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        spec.inspect {
            schemaDefinitions {
                // if the schema only has a BaseDefinitionComponent, we can replace it
                if (schemaDefinition.components.size == 1) {
                    components<BaseDefinitionComponent> {
                        val target = component.innerSchema.schemaDefinition
                        performRefactoring(SwapSchemaDefinitionRefactoring(schemaDefinition, target))
                        performRefactoring(DeleteSchemaDefinitionRefactoring(schemaDefinition))
                    }
                }
            }
        }
    }

}