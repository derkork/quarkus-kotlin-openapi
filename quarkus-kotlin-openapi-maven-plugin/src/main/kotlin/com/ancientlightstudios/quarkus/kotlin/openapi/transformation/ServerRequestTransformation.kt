package com.ancientlightstudios.quarkus.kotlin.openapi.transformation

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestIdentifierHint.requestIdentifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiRequest
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*

class ServerRequestTransformation : SpecTransformation {

    override fun TransformationContext.perform() {
        spec.solution.files
            .filterIsInstance<ServerRestController>()
            .forEach {
                it.source.inspect {
                    requests {
                        // the container class which contains all the input data of this request
                        val container = buildRequestContainer(request)

                        // the context class for this request
                        val context = buildRequestContext(request, container)
                        
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

    private fun TransformationContext.buildRequestContainer(request: OpenApiRequest): ServerRequestContainer? {
        if (!request.hasInputParameter()) {
            return null
        }

        val containerClassName = classNameOf(request.requestIdentifier, config.operationRequestPostfix)
        val container = ServerRequestContainer(
            ComponentName(containerClassName, config.packageName, ConflictResolution.Pinned),
            request
        )

        spec.solution.files.add(container)
        return container
    }

    private fun TransformationContext.buildRequestContext(
        request: OpenApiRequest, container: ServerRequestContainer?
    ): ServerRequestContext {
        val contextClassName = classNameOf(request.requestIdentifier, config.operationContextPostfix)
        val context = ServerRequestContext(
            ComponentName(contextClassName, config.packageName, ConflictResolution.Pinned),
            request.path,
            request.method,
            container,
            request
        )

        spec.solution.files.add(context)
        return context
    }

}