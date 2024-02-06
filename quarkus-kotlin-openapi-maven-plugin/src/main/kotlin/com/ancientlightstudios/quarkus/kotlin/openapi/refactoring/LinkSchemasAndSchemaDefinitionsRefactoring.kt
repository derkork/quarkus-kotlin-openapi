package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaDefinitionUsageHint.addUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.BaseDefinitionComponent

class LinkSchemasAndSchemaDefinitionsRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        spec.inspect {
            bundles {
                requests {
                    parameters { linkDefinitionToUsage(parameter.schema) }

                    body {
                        content { linkDefinitionToUsage(content.schema) }
                    }

                    responses {
                        headers { linkDefinitionToUsage(header.schema) }

                        body {
                            content { linkDefinitionToUsage(content.schema) }
                        }
                    }
                }
            }

            schemaDefinitions {
                nestedSchemas { linkDefinitionToUsage(usage) }
            }
        }
    }

    private fun linkDefinitionToUsage(usage: TransformableSchemaUsage) {
        usage.schemaDefinition.addUsage(usage)
    }

}