package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ForceNullableHint.forceNullable
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaDefinitionUsageHint.usage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaTypes
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.SomeOfComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.TypeComponent

class ReplaceNullableOverrideRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        spec.inspect {
            schemaDefinitions {
                // if the schema only has a TypeComponent which allows null and another
                // *OfComponent, we can replace it
                if (schemaDefinition.components.size == 2) {
                    components<TypeComponent> {
                        if (component.types.isNotEmpty() || component.nullable != true) {
                            return@components
                        }

                        components<SomeOfComponent> {
                            if (component.schemas.size == 1) {
                                val target = component.schemas.first().schemaDefinition
                                schemaDefinition.usage.forEach {
                                    it.forceNullable()
                                }
                                performRefactoring(SwapSchemaDefinitionRefactoring(schemaDefinition, target))
                                performRefactoring(DeleteSchemaDefinitionRefactoring(schemaDefinition))
                            }
                        }
                    }
                }
            }
        }
    }

}
