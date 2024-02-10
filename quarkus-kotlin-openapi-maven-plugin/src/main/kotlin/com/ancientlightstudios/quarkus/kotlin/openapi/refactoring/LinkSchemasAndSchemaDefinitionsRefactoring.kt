package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaDefinitionUsageHint.addUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaUsage

// after the parser stage, only schema usages know their schema definition. But traversing the other direction
// is also important for the refactoring.
class LinkSchemasAndSchemaDefinitionsRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        spec.inspect {
            bundles {
                requests {
                    parameters { linkDefinitionToUsage(parameter.schema) }

                    body {
                        linkDefinitionToUsage(body.content.schema)
                    }

                    responses {
                        headers { linkDefinitionToUsage(header.schema) }

                        body {
                            linkDefinitionToUsage(body.content.schema)
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