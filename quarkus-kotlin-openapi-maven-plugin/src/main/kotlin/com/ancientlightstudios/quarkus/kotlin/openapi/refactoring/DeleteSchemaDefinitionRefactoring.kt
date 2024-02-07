package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaDefinitionUsageHint.removeUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaDefinitionUsageHint.usage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class DeleteSchemaDefinitionRefactoring(private val current: TransformableSchemaDefinition) : SpecRefactoring {

    override fun RefactoringContext.perform() {
        if (current.usage.isNotEmpty()) {
            ProbableBug("Can't remove schema definition ${current.originPath}. It's still in use.")
        }

        // unlink any usage within this schema definition to other schema definitions
        current.inspect {
            nestedSchemas {
                usage.schemaDefinition.removeUsage(usage)
            }
        }

        spec.schemaDefinitions -= current
    }
}