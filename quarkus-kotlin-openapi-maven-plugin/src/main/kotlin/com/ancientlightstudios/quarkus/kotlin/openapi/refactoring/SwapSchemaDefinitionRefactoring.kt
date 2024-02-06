package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaDefinitionUsageHint.addUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaDefinitionUsageHint.clearUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaDefinitionUsageHint.usage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaDefinition

class SwapSchemaDefinitionRefactoring(
    private val current: TransformableSchemaDefinition,
    private val replacement: TransformableSchemaDefinition
) : SpecRefactoring {

    override fun RefactoringContext.perform() {
        current.usage.forEach {
            it.schemaDefinition = replacement
            replacement.addUsage(it)
        }

        current.clearUsage()
    }
}