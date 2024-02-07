package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ForceNullableHint.forceNullable
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaDefinitionUsageHint.usage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeUsageHint.typeUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeUsage

class SchemaUsageToTypeUsageRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        spec.inspect {
            schemaDefinitions {
                val typeDefinition = schemaDefinition.typeDefinition
                schemaDefinition.usage.forEach {
                    val nullable = it.forceNullable || typeDefinition.nullable
                    it.typeUsage = TypeUsage(nullable, typeDefinition)
                }
            }
        }
    }

}
