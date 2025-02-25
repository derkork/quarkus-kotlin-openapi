package com.ancientlightstudios.quarkus.kotlin.openapi.transformation

import com.ancientlightstudios.quarkus.kotlin.openapi.InterfaceType
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestBundleIdentifierHint.requestBundleIdentifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestIdentifierHint.requestIdentifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// generates the general solution model for a client implementation
class ClientTransformation : SpecTransformation {

    override fun TransformationContext.perform() {
        if (config.interfaceType != InterfaceType.CLIENT) {
            return
        }

        // the global dependency container
        val dependencyVogel = spec.solution.files
            .filterIsInstance<DependencyVogel>()
            .firstOrNull() ?: ProbableBug("solution dependency 'DependencyVogel' not found")

        spec.inspect {
            bundles {
                // the delegate interface for this bundle which is implemented by quarkus
                val delegateInterface = generateDelegateInterface()

                // the rest controller for this bundle which sits in front of the delegate and is used by the application
                val controller = generateRestController(delegateInterface, dependencyVogel)

                requests {
                    // the sealed interface with all responses and error cases for this request
                    val responseInterface = generateResponseInterface()

                    // the method for this request in the delegate interface
                    val delegateMethod = generateDelegateInterfaceMethod(delegateInterface)

                    // the method for this request in the rest controller
                    val controllerMethod = generateRestControllerMethod(controller, delegateMethod, responseInterface)

                    parameters {
                        val parameter = generateRequestParameter()
                        delegateMethod.parameters += parameter
                        controllerMethod.parameters += parameter
                    }

                    body {
                        val body = generateRequestBody()
                        delegateMethod.body = body
                        controllerMethod.body = body
                    }

                    responses {
                        // the implementation of the response interface for this response
                        val responseInterfaceImplementation =
                            generateResponseInterfaceImplementation(responseInterface)

                        headers {
                            responseInterfaceImplementation.headers += generateResponseHeader()
                        }

                        body {
                            responseInterfaceImplementation.body = generateResponseBody()
                        }
                    }
                }

            }
        }
    }

    context(TransformationContext)
    private fun RequestBundleInspection.generateDelegateInterface(): ClientDelegateInterface {
        val className = classNameOf(bundle.requestBundleIdentifier, "ClientDelegate")
        val result = ClientDelegateInterface(
            ComponentName(className, config.packageName, ConflictResolution.Pinned),
            "${config.interfaceName} client".toKebabCase(),
            config.pathPrefix,
            bundle
        )
        spec.solution.files.add(result)
        return result
    }

    context(TransformationContext)
    private fun RequestBundleInspection.generateRestController(
        delegateInterface: ClientDelegateInterface, dependencyVogel: DependencyVogel
    ): ClientRestController {
        val className = classNameOf(bundle.requestBundleIdentifier, "Client")
        val result = ClientRestController(
            ComponentName(className, config.packageName, ConflictResolution.Pinned),
            delegateInterface,
            dependencyVogel,
            bundle
        )
        spec.solution.files.add(result)
        return result
    }

    context(TransformationContext)
    private fun RequestInspection.generateResponseInterface(): ClientResponse {
        val httpClassName = classNameOf(request.requestIdentifier, config.operationHttpResponsePostfix)
        val httpResponse = ClientHttpResponse(
            ComponentName(httpClassName, config.packageName, ConflictResolution.Pinned)
        )

        val errorClassName = classNameOf(request.requestIdentifier, config.operationErrorPostfix)
        val errorResponse = ClientErrorResponse(
            ComponentName(errorClassName, config.packageName, ConflictResolution.Pinned)
        )

        val className = classNameOf(request.requestIdentifier, config.operationResponsePostfix)
        val result = ClientResponse(
            ComponentName(className, config.packageName, ConflictResolution.Pinned),
            httpResponse,
            errorResponse,
            request
        )
        spec.solution.files.add(result)
        return result
    }

    private fun RequestInspection.generateDelegateInterfaceMethod(
        delegateInterface: ClientDelegateInterface
    ): ClientDelegateInterfaceMethod {
        val methodName = methodNameOf(request.requestIdentifier)
        val result = ClientDelegateInterfaceMethod(methodName, request)
        delegateInterface.methods += result
        return result
    }

    private fun RequestInspection.generateRestControllerMethod(
        controller: ClientRestController,
        delegateInterfaceMethod: ClientDelegateInterfaceMethod,
        responseInterface: ClientResponse
    ): ClientRestControllerMethod {
        val result = ClientRestControllerMethod(
            delegateInterfaceMethod.name,
            delegateInterfaceMethod,
            responseInterface,
            request
        )
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

    private fun ResponseInspection.generateResponseInterfaceImplementation(
        responseInterface: ClientResponse
    ): ClientResponseImplementation {
        val className = classNameOf(response.responseCode.asMethodName())
        val result = ClientResponseImplementation(
            className,
            response
        )
        responseInterface.httpResponse.implementations += result
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

}