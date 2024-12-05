package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestBundleIdentifierHint.requestBundleIdentifier

/**
 * sets the [RequestBundleIdentifier][com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestBundleIdentifierHint]
 * that should be used for a request bundle in the generated code
 */
class SetRequestBundleIdentifierRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        spec.inspect {
            bundles {
                val tag = bundle.tag?.trim() ?: ""
                bundle.requestBundleIdentifier = "${config.interfaceName} $tag"
            }
        }
    }
}