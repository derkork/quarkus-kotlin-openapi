package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaDefinitionUsageHint.usage

class RemoveUnusedSchemaDefinitionsRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        // in case removing one schema unlinks a already visited schema, we do the whole check over and over again as
        // long as something was changed
        do {
            var changed = false
            spec.inspect {
                schemaDefinitions {
                    if (schemaDefinition.usage.isEmpty()) {
                        performRefactoring(DeleteSchemaDefinitionRefactoring(schemaDefinition))
                        changed = true
                    }
                }
            }
        } while (changed)
    }

}
