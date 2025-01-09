package com.ancientlightstudios.quarkus.kotlin.openapi.transformation

import com.ancientlightstudios.quarkus.kotlin.openapi.InterfaceType
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestBundleIdentifierHint.requestBundleIdentifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// generates the general solution model for a server implementation. This model can be extended by other
// transformers as needed
class ServerTransformation : SpecTransformation {

    override fun TransformationContext.perform() {
        if (config.interfaceType != InterfaceType.SERVER) {
            return
        }

        // the global dependency container
        val dependencyVogel = spec.solution.files
            .filterIsInstance<DependencyVogel>()
            .firstOrNull() ?: ProbableBug("solution dependency 'DependencyVogel' not found")

        spec.inspect {
            bundles {
                // the delegate interface which must be implemented with the business logic
                val delegateClassName = classNameOf(bundle.requestBundleIdentifier, "ServerDelegate")
                val delegateInterface = ServerDelegateInterface(
                    ComponentName(delegateClassName, config.packageName, ConflictResolution.Pinned),
                    bundle
                )
                spec.solution.files.add(delegateInterface)

                // the rest controller which sits between the quarkus request handler and our delegate interface
                val restClassName = classNameOf(bundle.requestBundleIdentifier, "Server")
                val restInterface = ServerRestController(
                    ComponentName(restClassName, config.packageName, ConflictResolution.Pinned),
                    config.pathPrefix,
                    delegateInterface,
                    dependencyVogel,
                    bundle
                )
                spec.solution.files.add(restInterface)
            }
        }
    }

}