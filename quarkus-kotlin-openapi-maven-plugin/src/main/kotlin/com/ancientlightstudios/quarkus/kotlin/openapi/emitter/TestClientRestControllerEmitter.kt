package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Handler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerResult
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.nestedTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.NullCheckExpression.Companion.nullCheck
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ParameterKind
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.methodNameOf

class TestClientRestControllerEmitter : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.solution.files
            .filterIsInstance<TestClientRestController>()
            .forEach { emitFile(it) }
    }

    private fun EmitterContext.emitFile(controller: TestClientRestController) {
        kotlinFile(controller.name.asTypeName()) {
            registerImports(Library.All)
            registerImports(config.additionalImports())

            kotlinClass(name) {
                kotlinMember("dependencyVogel", controller.dependencyVogel.name.asTypeReference())
                kotlinMember(
                    "specBuilder", KotlinDelegateTypeReference(null, RestAssured.RequestSpecification.asTypeReference())
                )

                controller.methods.forEach { method ->
                    emitSafeMethod(method)
                    emitUnsafeMethod(method)
                    emitRawMethod(method)
                }
            }
        }
    }

    context(EmitterContext)
    private fun KotlinClass.emitSafeMethod(method: TestClientRestControllerMethod) {
        kotlinMethod(methodNameOf(method.name, "Safe"), bodyAsAssignment = true) {
            val statements = mutableListOf<KotlinExpression>()
            val pathParams = mutableListOf<KotlinExpression>()

            method.parameters.forEach { parameter ->
                val typeInfo = getHandler<TestClientRestControllerHandler, TypeInfo> {
                    TestClientRestControllerHandlerContext.parameterType(parameter)
                }

                kotlinParameter(parameter.name, typeInfo.type, typeInfo.defaultValue.toKotlinExpression())

                val parameterIdentifier = parameter.name.identifier()
                if (parameter.kind == ParameterKind.Path) {
                    pathParams.add(parameterIdentifier)
                } else {
                    statements.add(invoke(parameterIdentifier, parameterIdentifier))
                }
            }

            method.body?.let { body ->
                val typeInfo = getHandler<TestClientRestControllerHandler, TypeInfo> {
                    TestClientRestControllerHandlerContext.bodyType(body)
                }

                kotlinParameter(body.name, typeInfo.type, typeInfo.defaultValue.toKotlinExpression())

                val bodyIdentifier = body.name.identifier()
                statements.add(invoke(bodyIdentifier, bodyIdentifier))

            }

            invoke(methodNameOf(method.name, "Unsafe"), *pathParams.toTypedArray()) {
                statements.forEach {
                    it.statement()
                }
            }.statement()
        }
    }

    context(EmitterContext)
    private fun KotlinClass.emitUnsafeMethod(method: TestClientRestControllerMethod) {
        kotlinMethod(methodNameOf(method.name, "Unsafe"), bodyAsAssignment = true) {

            val pathParams = method.parameters.filter { it.kind == ParameterKind.Path }
                .map { parameter ->
                    val typeInfo = getHandler<TestClientRestControllerHandler, TypeInfo> {
                        TestClientRestControllerHandlerContext.parameterType(parameter)
                    }
                    kotlinParameter(parameter.name, typeInfo.type, typeInfo.defaultValue.toKotlinExpression())

                    getHandler<TestClientRestControllerHandler, KotlinExpression> {
                        TestClientRestControllerHandlerContext.pathParameterSerialization(parameter)
                    }
                }

            kotlinParameter(
                "block",
                KotlinDelegateTypeReference(method.builder.name.asTypeReference(), Kotlin.Unit.asTypeReference()),
                emptyLambda()
            )

            invoke(methodNameOf(method.name, "Raw"), *pathParams.toTypedArray()) {
                invoke(
                    method.builder.name.identifier(),
                    "this".identifier(),
                    "dependencyVogel".identifier()
                )
                    .invoke("apply", "block".identifier())
                    .property("requestSpecification")
                    .statement()
            }.statement()
        }
    }

    context(EmitterContext)
    private fun KotlinClass.emitRawMethod(method: TestClientRestControllerMethod) {
        kotlinMethod(methodNameOf(method.name, "Raw"), returnType = method.validator.name.asTypeReference()) {

            val pathParams = method.parameters.filter { it.kind == ParameterKind.Path }
                .map { parameter ->
                    kotlinParameter(parameter.name, Kotlin.Any.asTypeReference())
                    invoke(Kotlin.Pair.identifier(), parameter.name.literal(), parameter.name.identifier())
                }

            kotlinParameter(
                "block",
                KotlinDelegateTypeReference(
                    RestAssured.RequestSpecification.asTypeReference(),
                    RestAssured.RequestSpecification.asTypeReference()
                )
            )

            val outputStream = invoke(Kotlin.ByteArrayOutputStream.identifier())
                .declaration("outputStream")
            val printStream = invoke(Kotlin.PrintStream.identifier(), outputStream.identifier())
                .declaration("printStream")

            val result = TryCatchExpression.tryExpression {
                val pathParamMap = InvocationExpression.invoke(
                    "mapOf", *pathParams.toTypedArray(),
                    genericTypes = listOf(Kotlin.String.asTypeReference(), Kotlin.Any.asTypeReference())
                )

                var validatableResponse = InvocationExpression.invoke("specBuilder").wrap()
                    .invoke(
                        "filter", InvocationExpression.invoke(
                            Library.RequestLoggingFilter.identifier(),
                            printStream.identifier()
                        )
                    ).wrap()
                    .invoke(
                        "filter", InvocationExpression.invoke(
                            Library.ResponseLoggingFilter.identifier(),
                            printStream.identifier()
                        )
                    ).wrap()

                method.body?.let { body ->
                    // produces
                    // .contentType("<rawContentType>")
                    validatableResponse = validatableResponse
                        .invoke("contentType", body.content.rawContentType.literal())
                        .wrap()
                }

                val validatableResponseVariable = validatableResponse
                    .invoke("run", "block".identifier()).wrap()
                    .invoke(
                        method.restMethod.value,
                        method.restPath.literal(),
                        pathParamMap
                    )  // TODO: prefix
                    .wrap()
                    .invoke("then").wrap()
                    .invoke("extract")
                    .declaration("validatableResponse")

                emitResponseConversion(method, validatableResponseVariable)

                val errorClass = method.response.errorResponse.name.asTypeName()

                // produces
                // catch (_: TimeoutException) {
                //     AddMovieRatingError.RequestErrorTimeout()
                // }
                catchBlock(Misc.TimeoutException, ignoreVariable = true) {
                    // otherwise the tryExpression will be picked as the receiver which will produce a compiler error
                    // due to the dsl annotation or an endless recursion without the annotation
                    InvocationExpression.invoke(errorClass.nestedTypeName("RequestErrorTimeout").identifier())
                        .statement()
                }

                // produces
                // catch (e: Exception) {
                //     AddMovieRatingError.RequestErrorUnknown(e)
                // }
                catchBlock(Kotlin.Exception) {
                    InvocationExpression.invoke(
                        errorClass.nestedTypeName("RequestErrorUnknown").identifier(), "e".identifier()
                    ).statement()
                }
            }.declaration("result")
            invoke(method.validator.name.identifier(), result.identifier(), outputStream.identifier()).returnStatement()
        }
    }

    context(EmitterContext)
    private fun StatementAware.emitResponseConversion(
        method: TestClientRestControllerMethod, validatableResponse: String
    ) {
        val successType = method.response.httpResponse.name.asTypeName()
        val errorType = method.response.errorResponse.name.asTypeName()

        // produces
        // val statusCode = <validatableResponse>.statusCode()
        val statusCode = validatableResponse.identifier().invoke("statusCode").declaration("statusCode")

        // produces
        // val responseMaybe: Maybe<[ResponseContainerClass]> = when (statusCode) {
        //     ...
        // }
        WhenExpression.whenExpression(statusCode.identifier()) {
            val responses = method.response.httpResponse.implementations

            // generate options for all known status codes
            responses.filter { it.responseCode is ResponseCode.HttpStatusCode }.forEach {
                val optionValue = (it.responseCode as ResponseCode.HttpStatusCode).value.literal()
                generateResponseOption(
                    successType.nestedTypeName(it.name), optionValue, false, it
                )
            }

            // generate option for the default status or fallback otherwise
            val defaultResponse = responses.firstOrNull { it.responseCode == ResponseCode.Default }
            when (defaultResponse) {
                null -> generateFallbackResponseOption(errorType)
                else -> generateResponseOption(
                    successType.nestedTypeName(defaultResponse.name), "else".identifier(), true, defaultResponse
                )
            }
        }.declaration("responseMaybe", Library.Maybe.asTypeReference(method.response.name.asTypeReference()))


        // produces
        // when(responseMaybe) {
        //    ...
        // }
        WhenExpression.whenExpression("responseMaybe".identifier()) {
            // produces
            // is Maybe.Success -> responseMaybe.value
            optionBlock(AssignableExpression.assignable(Library.MaybeSuccess.asTypeReference())) {
                "responseMaybe".identifier().property("value").statement()
            }

            // produces
            // is Maybe.Failure -> {
            //     val errors = responseMaybe.errors.joinToString { "${it.path}: ${it.message}" }
            //     <ResponseObject>(errors, validatableResponse.response())
            // }
            optionBlock(AssignableExpression.assignable(Library.MaybeFailure.asTypeReference())) {
                "responseMaybe".identifier().property("errors")
                    .invoke("joinToString") {
                        "\${it.path}: \${it.message}".literal().statement()
                    }.declaration("errors")

                InvocationExpression.invoke(
                    errorType.nestedTypeName("ResponseError").identifier(),
                    "errors".identifier(),
                    "validatableResponse".identifier().invoke("response")
                ).statement()
            }
        }.statement()
    }

    // build something like
    //
    // with a body
    //
    // RestResponse.Status.<ResponseName> -> {
    //
    // }
    //
    // without a body
    //
    // RestResponse.Status.<ResponseName> -> Maybe.Success("response.body", <ResponseObject>)
    context(EmitterContext)
    private fun WhenOptionAware.generateResponseOption(
        name: KotlinTypeName,
        optionValue: KotlinExpression,
        withStatusCode: Boolean,
        implementation: ClientResponseImplementation
    ) = optionBlock(optionValue) {
        val objectParts = mutableListOf<InstantiationParameter>()
        if (withStatusCode) {
            objectParts += PlainParameter("statusCode")
        }

        implementation.headers.forEach { header ->
            objectParts += emitHeaderParameter(header)
        }

        implementation.body?.let { body ->
            objectParts += emitBody(body)
        }

        allToObject("response".literal(), name, objectParts).statement()
    }

    context(EmitterContext)
    private fun WhenOption.emitHeaderParameter(header: ResponseHeader): InstantiationParameter {
        // produces
        //
        // validatableResponse.headers().getValues("<headerName>")[.firstOrNull()]
        var headerValueExpression: KotlinExpression =
            "validatableResponse".identifier().invoke("headers")
                .invoke("getValues", header.name.literal())

        if (header.content.model.instance !is CollectionModelInstance) {
            headerValueExpression = headerValueExpression.invoke("firstOrNull")
        }

        val context = object : TestClientRestControllerResponseHandlerContext {
            override fun addStatement(statement: KotlinStatement) = this@emitHeaderParameter.addStatement(statement)
        }

        return getHandler<TestClientRestControllerResponseHandler, InstantiationParameter> {
            context.emitHeader(header, headerValueExpression)
        }
    }

    context(EmitterContext)
    private fun WhenOption.emitBody(body: ResponseBody): InstantiationParameter {
        // produces
        // val entity = validatableResponse.body()
        val entity = "validatableResponse".identifier().invoke("body").declaration("entity")

        val context = object : TestClientRestControllerResponseHandlerContext {
            override fun addStatement(statement: KotlinStatement) = this@emitBody.addStatement(statement)
        }

        return getHandler<TestClientRestControllerResponseHandler, InstantiationParameter> {
            context.emitBody(body, entity.identifier())
        }
    }

    // generates
    // else -> Maybe.Success("response.body", <ResponseObject>("unknown status code ${statusCode.name}", response))
    private fun WhenOptionAware.generateFallbackResponseOption(errorType: KotlinTypeName) {
        optionBlock("else".identifier()) {
            // produces
            // <ResponseObject>("unknown status code ${statusCode.name}", validatableResponse.response())
            val newInstance = invoke(
                errorType.nestedTypeName("ResponseError").identifier(),
                "unknown status code \${statusCode}".literal(),
                "validatableResponse".identifier().invoke("response")
            )
            // produces
            // Maybe.Success("response.body", <newInstance>)
            invoke(Library.MaybeSuccess.identifier(), "response.body".literal(), newInstance).statement()
        }
    }

}

object TestClientRestControllerHandlerContext

interface TestClientRestControllerHandler : Handler {

    /**
     * Returns the type for a request parameter in the test client rest controller. The type should reflect the
     * nullability of the model even if the value will never be nullable after serialization. Allows maximum
     * flexibility for the code which is providing values.
     */
    fun TestClientRestControllerHandlerContext.parameterType(parameter: RequestParameter): HandlerResult<TypeInfo>

    /**
     * Returns the type for a request body in the test client rest controller. The type should reflect the
     * nullability of the model even if the value will never be nullable after serialization. Allows maximum
     * flexibility for the code which is providing values.
     */
    fun TestClientRestControllerHandlerContext.bodyType(body: RequestBody): HandlerResult<TypeInfo>

    fun TestClientRestControllerHandlerContext.pathParameterSerialization(parameter: RequestParameter):
            HandlerResult<KotlinExpression>

}

interface TestClientRestControllerResponseHandlerContext : StatementAware

interface TestClientRestControllerResponseHandler : Handler {

    fun TestClientRestControllerResponseHandlerContext.emitHeader(header: ResponseHeader, source: KotlinExpression):
            HandlerResult<InstantiationParameter>

    fun TestClientRestControllerResponseHandlerContext.emitBody(body: ResponseBody, source: KotlinExpression):
            HandlerResult<InstantiationParameter>

}

