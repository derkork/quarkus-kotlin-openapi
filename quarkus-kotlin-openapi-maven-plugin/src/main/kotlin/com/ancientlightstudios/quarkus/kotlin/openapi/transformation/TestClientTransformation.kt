package com.ancientlightstudios.quarkus.kotlin.openapi.transformation

import com.ancientlightstudios.quarkus.kotlin.openapi.InterfaceType
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestBundleIdentifierHint.requestBundleIdentifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestIdentifierHint.requestIdentifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// generates the general solution model for a test-client implementation
class TestClientTransformation : SpecTransformation {

    override fun TransformationContext.perform() {
        if (config.interfaceType != InterfaceType.TEST_CLIENT) {
            return
        }

        // the global dependency container
        val dependencyVogel = spec.solution.files
            .filterIsInstance<DependencyVogel>()
            .firstOrNull() ?: ProbableBug("solution dependency 'DependencyVogel' not found")

        spec.inspect {
            bundles {
                // the rest controller for this bundle which is used by the tests
                val controller = generateRestController(dependencyVogel)

                requests {
                    // the sealed interface with all responses and error cases for this request
                    val responseInterface = generateResponseInterface()

                    // the builder to generate unsafe requests and stuff
                    val builder = generateRequestBuilder(dependencyVogel)

                    // the response validator for restassured
                    val validator = generateResponseValidator(responseInterface)

                    // the method for this request in the rest controller
                    val controllerMethod =
                        generateRestControllerMethod(controller, builder, responseInterface, validator)

                    parameters {
                        val parameter = generateRequestParameter()
                        controllerMethod.parameters += parameter
                        builder.parameters += parameter
                    }

                    body {
                        val body = generateRequestBody()
                        controllerMethod.body = body
                        builder.body = body
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
    private fun RequestBundleInspection.generateRestController(dependencyVogel: DependencyVogel): TestClientRestController {
        val className = classNameOf(bundle.requestBundleIdentifier, "TestClient")
        val result = TestClientRestController(
            ComponentName(className, config.packageName, ConflictResolution.Pinned),
            dependencyVogel,
            bundle
        )
        spec.solution.files.add(result)
        return result
    }

    context(TransformationContext)
    private fun RequestInspection.generateRequestBuilder(dependencyVogel: DependencyVogel): TestClientRequestBuilder {
        val className = classNameOf(request.requestIdentifier, config.operationBuilderPostfix)
        val result = TestClientRequestBuilder(
            ComponentName(className, config.packageName, ConflictResolution.Pinned),
            dependencyVogel,
            request
        )
        spec.solution.files.add(result)
        return result
    }

    context(TransformationContext)
    private fun RequestInspection.generateResponseValidator(response: ClientResponse): TestClientResponseValidator {
        val className = classNameOf(request.requestIdentifier, config.operationValidatorPostfix)
        val result = TestClientResponseValidator(
            ComponentName(className, config.packageName, ConflictResolution.Pinned),
            response,
            request
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

    private fun RequestInspection.generateRestControllerMethod(
        controller: TestClientRestController,
        builder: TestClientRequestBuilder,
        responseInterface: ClientResponse,
        validator: TestClientResponseValidator
    ): TestClientRestControllerMethod {
        val methodName = methodNameOf(request.requestIdentifier)
        val result = TestClientRestControllerMethod(methodName, builder, responseInterface, validator, request)
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
        "safeBody", contentModelFor(body.content, Direction.Down, body.required), body
    )

}