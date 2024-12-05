package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestIdentifierHint.requestIdentifier

/**
 * sets the [RequestIdentifier][com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestIdentifierHint]
 * that should be used for a request in the generated code
 */
class SetRequestIdentifierRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        spec.inspect {
            bundles {
                requests {
                    val value = request.operationId
                    request.requestIdentifier = when {
                        value.isNullOrBlank() -> "${request.method.name} ${request.path}"
                        else -> value
                    }
                }
            }
        }
    }
}