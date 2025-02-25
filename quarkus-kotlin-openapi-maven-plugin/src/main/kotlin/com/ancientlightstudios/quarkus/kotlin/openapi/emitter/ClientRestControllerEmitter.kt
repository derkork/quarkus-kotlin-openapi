package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Handler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerResult
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.nestedTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TryCatchExpression.Companion.tryExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.WhenExpression.Companion.whenExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*

class ClientRestControllerEmitter : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.solution.files
            .filterIsInstance<ClientRestController>()
            .forEach { emitFile(it) }
    }

    private fun EmitterContext.emitFile(restController: ClientRestController) {
        kotlinFile(restController.name.asTypeName()) {
            registerImports(Library.All)
            registerImports(config.additionalImports())

            kotlinClass(name) {
                kotlinAnnotation(Jakarta.ApplicationScoped)

                kotlinMember("delegate", restController.delegate.name.asTypeReference()) {
                    kotlinAnnotation(Misc.RestClient)
                }

                kotlinMember("dependencyVogel", restController.dependencyVogel.name.asTypeReference())

                restController.methods.forEach {
                    emitRequest(it)
                }
            }
        }
    }

    context(EmitterContext)
    private fun KotlinClass.emitRequest(method: ClientRestControllerMethod) {
        val successType = method.response.httpResponse.name.asTypeName()
        val errorType = method.response.errorResponse.name.asTypeName()

        kotlinMethod(method.name, true, method.response.name.asTypeReference()) {
            val delegateParts = mutableListOf<KotlinExpression>()
            tryExpression {

                val context = object : ClientRestControllerRequestHandlerContext {
                    override fun addParameter(parameter: KotlinParameter) = this@kotlinMethod.addParameter(parameter)
                    override fun addStatement(statement: KotlinStatement) = this@tryExpression.addStatement(statement)
                }

                method.parameters.forEach { parameter ->
                    delegateParts += getHandler<ClientRestControllerRequestHandler, List<KotlinExpression>> {
                        context.emitParameter(parameter)
                    }
                }

                method.body?.let { body ->
                    delegateParts += getHandler<ClientRestControllerRequestHandler, List<KotlinExpression>> {
                        context.emitBody(body)
                    }
                }

                tryExpression {
                    // produces
                    // delegate.<methodName>(<serializedParameters ...>).toResponse()
                    "delegate".identifier().invoke(method.delegateMethod.name, *delegateParts.toTypedArray())
                        .invoke("toResponse").statement()

                    // produces
                    // catch (e: WebApplicationException) {
                    //     e.response
                    // }
                    catchBlock(Jakarta.WebApplicationException) {
                        "e".identifier().property("response").statement()
                    }
                }.declaration("response")

                // produces
                // val statusCode = response.status
                val statusCode = "response".identifier().property("status").declaration("statusCode")

                // produces
                // val responseMaybe: Maybe<[ResponseContainerClass]> = when (statusCode) {
                //     ...
                // }
                whenExpression(statusCode.identifier()) {
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
                whenExpression("responseMaybe".identifier()) {
                    // produces
                    // is Maybe.Success -> responseMaybe.value
                    optionBlock(AssignableExpression.assignable(Library.MaybeSuccess.asTypeReference())) {
                        "responseMaybe".identifier().property("value").statement()
                    }

                    // produces
                    // is Maybe.Failure -> {
                    //     val errors = responseMaybe.errors.joinToString { "${it.path}: ${it.message}" }
                    //     <ResponseObject>(errors, response)
                    // }
                    optionBlock(AssignableExpression.assignable(Library.MaybeFailure.asTypeReference())) {
                        "responseMaybe".identifier().property("errors")
                            .invoke("joinToString") {
                                "\${it.path}: \${it.message}".literal().statement()
                            }.declaration("errors")

                        InvocationExpression.invoke(
                            errorType.nestedTypeName("ResponseError").identifier(),
                            "errors".identifier(),
                            "response".identifier()
                        ).statement()
                    }
                }.statement()

                // produces
                // catch (_: TimeoutException) {
                //     <errorType>.RequestErrorTimeout()
                // }
                catchBlock(Misc.TimeoutException, ignoreVariable = true) {
                    // otherwise the tryExpression will be picked as the receiver which will produce a compiler error
                    // due to the dsl annotation or an endless recursion without the annotation
                    InvocationExpression.invoke(errorType.nestedTypeName("RequestErrorTimeout").identifier())
                        .statement()
                }

                // produces
                // catch (e: Exception) {
                //     <errorType>.RequestErrorUnknown(e)
                // }
                catchBlock(Kotlin.Exception) {
                    InvocationExpression.invoke(
                        errorType.nestedTypeName("RequestErrorUnknown").identifier(), "e".identifier()
                    ).statement()
                }
            }.returnStatement()
        }
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
    // RestResponse.Status.<ResponseName> -> Maybe.Success("response", <ResponseObject>)
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
        // response.stringHeaders.get[First]("<headerName>")
        val methodName = when (header.content.model.instance) {
            is CollectionModelInstance -> "get"
            else -> "getFirst"
        }
        val headerValueExpression = "response".identifier()
            .property("stringHeaders")
            .invoke(methodName, header.name.literal())

        val context = object : ClientRestControllerResponseHandlerContext {
            override fun addStatement(statement: KotlinStatement) = this@emitHeaderParameter.addStatement(statement)
        }

        return getHandler<ClientRestControllerResponseHandler, InstantiationParameter> {
            context.emitHeader(header, headerValueExpression)
        }
    }

    context(EmitterContext)
    private fun WhenOption.emitBody(body: ResponseBody): InstantiationParameter {
        // produces
        // val entity = when(response.hasEntity()) {
        //     true -> response.readEntity(ByteArray::class.java)
        //     false-> null
        // }
        val entity = whenExpression("response".identifier().invoke("hasEntity")) {
            optionBlock(true.literal()) {
                "response".identifier().invoke(
                    "readEntity",
                    Kotlin.ByteArray.identifier().functionReference("class.java")
                ).statement()
            }
            optionBlock(false.literal()) {
                nullLiteral().statement()
            }
        }.declaration("entity")

        val context = object : ClientRestControllerResponseHandlerContext {
            override fun addStatement(statement: KotlinStatement) = this@emitBody.addStatement(statement)
        }

        return getHandler<ClientRestControllerResponseHandler, InstantiationParameter> {
            context.emitBody(body, entity.identifier())
        }
    }

    // generates
    // else -> Maybe.Success("response", <ResponseObject>("unknown status code ${statusCode.name}", response))
    private fun WhenOptionAware.generateFallbackResponseOption(errorType: KotlinTypeName) {
        optionBlock("else".identifier()) {
            // produces
            // <ResponseObject>("unknown status code ${statusCode.name}", response)
            val newInstance = invoke(
                errorType.nestedTypeName("ResponseError").identifier(),
                "unknown status code \$statusCode".literal(),
                "response".identifier()
            )
            // produces
            // Maybe.Success("response", <newInstance>)
            invoke(Library.MaybeSuccess.identifier(), "response".literal(), newInstance).statement()
        }
    }

}

interface ClientRestControllerRequestHandlerContext : ParameterAware, StatementAware {

    /**
     * Generates the standard property for a request parameter or request body in the client rest controller.
     * The type should reflect the nullability of the model even if the value will never be nullable after
     * serialization. Allows maximum flexibility for the code which is providing values.
     */
    fun emitProperty(name: String, type: KotlinTypeReference, defaultValue: DefaultValue) =
        kotlinParameter(name, type, defaultValue.toKotlinExpression())

}

interface ClientRestControllerRequestHandler : Handler {

    /**
     * Emits the property for a request parameter in the client rest controller. The type of the property should reflect
     * the nullability of the model even if the value will never be nullable after serialization. Allows maximum
     * flexibility for the code which is providing values.
     */
    fun ClientRestControllerRequestHandlerContext.emitParameter(parameter: RequestParameter):
            HandlerResult<List<KotlinExpression>>

    /**
     * Emits the property for a request body in the client rest controller. The type of the property should reflect
     * the nullability of the model even if the value will never be nullable after serialization. Allows maximum
     * flexibility for the code which is providing values.
     */
    fun ClientRestControllerRequestHandlerContext.emitBody(body: RequestBody): HandlerResult<List<KotlinExpression>>

}

interface ClientRestControllerResponseHandlerContext : StatementAware

interface ClientRestControllerResponseHandler : Handler {

    /**
     * Emits the deserialization statement for a response header.
     */
    fun ClientRestControllerResponseHandlerContext.emitHeader(header: ResponseHeader, source: KotlinExpression):
            HandlerResult<InstantiationParameter>

    /**
     * Emits the deserialization statement for a response body.
     */
    fun ClientRestControllerResponseHandlerContext.emitBody(body: ResponseBody, source: KotlinExpression):
            HandlerResult<InstantiationParameter>

}
