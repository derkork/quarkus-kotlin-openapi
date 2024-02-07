package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaDefinitionUsageHint.addUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaUsage

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