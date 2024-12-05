package com.ancientlightstudios.quarkus.kotlin.openapi.transformation

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestIdentifierHint.requestIdentifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*

class ServerRequestTransformation : SpecTransformation {

    override fun TransformationContext.perform() {
        spec.solution.files
            .filterIsInstance<ServerRestController>()
            .forEach {
                it.source.inspect {
                    requests {
                        // the context class for this request
                        val contextClassName = classNameOf(request.requestIdentifier, "Context")
                        val context = ServerRequestContext(
                            FileName(contextClassName, config.packageName, ConflictResolution.Pinned),
                            request
                        )
                        spec.solution.files.add(context)

                        val requestMethodName = methodNameOf(request.requestIdentifier)
                        // the method for this request in the delegate interface
                        val delegateMethod = ServerDelegateInterfaceMethod(
                            requestMethodName, context, request
                        )
                        it.delegate.methods.add(delegateMethod)

                        // the method for this request in the rest controller
                        val restMethod = ServerRestControllerMethod(
                            requestMethodName, request.path, request.method, delegateMethod, request
                        )
                        it.methods.add(restMethod)
                    }
                }
            }
    }

}