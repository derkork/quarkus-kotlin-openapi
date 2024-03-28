package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.DownTypeDefinitionHint.downTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeUsageHint.typeUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.UpTypeDefinitionHint.upTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeUsage

class AssignTypesToRequestsRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        spec.inspect {
            bundles {
                requests {
                    parameters {
                        parameter.typeUsage = TypeUsage(parameter.required, parameter.content.schema.upTypeDefinition)
                    }

                    body {
                        body.content.typeUsage = TypeUsage(body.required, body.content.schema.upTypeDefinition)
                    }

                    responses {
                        headers {
                            header.typeUsage = TypeUsage(header.required, header.content.schema.downTypeDefinition)
                        }

                        body {
                            body.content.typeUsage = TypeUsage(body.required, body.content.schema.downTypeDefinition)
                        }
                    }
                }
            }
        }
    }

}
