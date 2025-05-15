package com.ancientlightstudios.quarkus.kotlin.openapi.transformation

import com.ancientlightstudios.quarkus.kotlin.openapi.InterfaceType
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestBundleIdentifierHint.requestBundleIdentifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestIdentifierHint.requestIdentifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// generates the general solution model for a server implementation
class ServerTransformation : SpecTransformation {

    override fun TransformationContext.perform() {
        if (config.interfaceType != InterfaceType.SERVER) {
            return
        }

        // the global dependency container
        val dependencyContainer = spec.solution.files
            .filterIsInstance<DependencyContainer>()
            .firstOrNull() ?: ProbableBug("solution dependency 'DependencyContainer' not found")

        spec.inspect {
            val knownInterfaces = generateResponseInterfaces()

            bundles {
                // the delegate interface for this bundle which must be implemented with the business logic
                val delegateInterface = generateDelegateInterface()

                // the rest controller for this bundle which sits between the quarkus request handler and our delegate interface
                val controller = generateRestController(delegateInterface, dependencyContainer)

                requests {
                    // the container class which contains the input data (parameter and body) for this request
                    val container = generateRequestContainer()

                    // the context class which contains the valid responses for this request
                    val context = generateRequestContext(container, dependencyContainer)

                    // the method for this request in the delegate interface
                    val delegateMethod = generateDelegateInterfaceMethod(delegateInterface, context)

                    // the method for this request in the rest controller
                    val controllerMethod = generateRestControllerMethod(controller, delegateMethod)

                    parameters {
                        val parameter = generateRequestParameter()
                        container?.parameters?.add(parameter)
                        controllerMethod.parameters += parameter
                    }

                    body {
                        val body = generateRequestBody()
                        container?.body = body
                        controllerMethod.body = body
                    }

                    responses {
                        // the method for this response in the request context
                        val responseMethod = generateRequestContextResponseMethod(context, knownInterfaces)

                        headers {
                            responseMethod.headers += generateResponseHeader()
                        }

                        body {
                            responseMethod.body = generateResponseBody()
                        }
                    }
                }
            }
        }
    }

    context(TransformationContext)
    private fun SpecInspection.generateResponseInterfaces(): Map<String, ServerResponseInterface> {
        val result = mutableMapOf<String, ServerResponseInterface>()

        bundles {
            requests {
                responses {
                    response.interfaceName?.let {
                        // shared responses are duplicated into each request by the open api parser. So it's
                        // not possible to decide here, if this is the same response or another response with
                        // just the same interface name. First case is ok and we can ignore the duplicates.
                        // For the second case we have to relly on the compiler to detect incompatibilities.
                        // Maybe changing the behaviour of the parser would be a good idea, but introduces other
                        // issues in the generator pipeline
                        if (!result.containsKey(it)) {
                            val responseInterface = ServerResponseInterface(
                                ComponentName(it, config.packageName, ConflictResolution.Requested),
                                response.responseCode.asServerMethodName(),
                                response
                            )

                            headers {
                                responseInterface.headers += generateResponseHeader()
                            }

                            body {
                                responseInterface.body = generateResponseBody()
                            }

                            result[it] = responseInterface
                            spec.solution.files.add(responseInterface)
                        }
                    }
                }
            }
        }
        return result
    }

    context(TransformationContext)
    private fun RequestBundleInspection.generateDelegateInterface(): ServerDelegateInterface {
        val className = classNameOf(bundle.requestBundleIdentifier, "ServerDelegate")
        val result = ServerDelegateInterface(
            ComponentName(className, config.packageName, ConflictResolution.Pinned),
            bundle
        )
        spec.solution.files.add(result)
        return result
    }

    context(TransformationContext)
    private fun RequestBundleInspection.generateRestController(
        delegateInterface: ServerDelegateInterface, dependencyContainer: DependencyContainer
    ): ServerRestController {
        val className = classNameOf(bundle.requestBundleIdentifier, "Server")
        val result = ServerRestController(
            ComponentName(className, config.packageName, ConflictResolution.Pinned),
            config.pathPrefix,
            delegateInterface,
            dependencyContainer,
            bundle
        )
        spec.solution.files.add(result)
        return result
    }

    context(TransformationContext)
    private fun RequestInspection.generateRequestContainer(): ServerRequestContainer? {
        if (!request.hasInputParameter()) {
            return null
        }

        val className = classNameOf(request.requestIdentifier, config.operationRequestPostfix)
        val result = ServerRequestContainer(
            ComponentName(className, config.packageName, ConflictResolution.Pinned),
            request
        )
        spec.solution.files.add(result)
        return result
    }

    context(TransformationContext)
    private fun RequestInspection.generateRequestContext(
        requestContainer: ServerRequestContainer?, dependencyContainer: DependencyContainer
    ): ServerRequestContext {
        val className = classNameOf(request.requestIdentifier, config.operationContextPostfix)
        val result = ServerRequestContext(
            ComponentName(className, config.packageName, ConflictResolution.Pinned),
            requestContainer,
            dependencyContainer,
            request
        )
        spec.solution.files.add(result)
        return result
    }

    private fun RequestInspection.generateDelegateInterfaceMethod(
        delegateInterface: ServerDelegateInterface, context: ServerRequestContext
    ): ServerDelegateInterfaceMethod {
        val methodName = methodNameOf(request.requestIdentifier)
        val result = ServerDelegateInterfaceMethod(methodName, context, request)
        delegateInterface.methods += result
        return result
    }

    private fun RequestInspection.generateRestControllerMethod(
        controller: ServerRestController, delegateInterfaceMethod: ServerDelegateInterfaceMethod
    ): ServerRestControllerMethod {
        val result = ServerRestControllerMethod(delegateInterfaceMethod.name, delegateInterfaceMethod, request)
        controller.methods += result
        return result
    }

    context(TransformationContext)
    private fun ParameterInspection.generateRequestParameter() = RequestParameter(
        variableNameOf(parameter.name),
        contentModelFor(parameter.content, Direction.Up, parameter.required),
        parameter
    )

    context(TransformationContext)
    private fun BodyInspection.generateRequestBody() = RequestBody(
        "body",
        contentModelFor(body.content, Direction.Up, body.required),
        body
    )

    private fun ResponseInspection.generateRequestContextResponseMethod(
        context: ServerRequestContext,
        knownInterfaces: Map<String, ServerResponseInterface>
    ): ServerRequestContextResponseMethod {
        val responseInterface = response.interfaceName?.let { knownInterfaces[it] }
        val result = ServerRequestContextResponseMethod(
            response.responseCode.asServerMethodName(),
            responseInterface,
            response
        )
        context.methods += result
        return result
    }

    context(TransformationContext)
    private fun ResponseHeaderInspection.generateResponseHeader() = ResponseHeader(
        variableNameOf(header.name),
        contentModelFor(header.content, Direction.Down, header.required),
        header
    )

    context(TransformationContext)
    private fun BodyInspection.generateResponseBody() = ResponseBody(
        "body", contentModelFor(body.content, Direction.Down, body.required), body
    )

    private fun ResponseCode.asServerMethodName() = when(this) {
        is ResponseCode.Default -> "defaultStatus"
        else -> asMethodName()
    }

}