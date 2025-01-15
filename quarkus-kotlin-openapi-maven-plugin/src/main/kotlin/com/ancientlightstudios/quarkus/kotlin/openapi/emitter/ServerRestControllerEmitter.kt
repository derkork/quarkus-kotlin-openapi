package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Handler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerResult
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TryCatchExpression.Companion.tryExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.RequestBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.RequestParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ServerRestController
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ServerRestControllerMethod

class ServerRestControllerEmitter : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.solution.files
            .filterIsInstance<ServerRestController>()
            .forEach { emitFile(it) }
    }

    private fun EmitterContext.emitFile(restController: ServerRestController) {
        kotlinFile(restController.name.asTypeName()) {
            registerImports(Library.All)
            registerImports(config.additionalImports())

            kotlinClass(name) {
                if (config.onlyProfile.isNotBlank()) {
                    kotlinAnnotation(Quarkus.IfBuildProfileAnnotation, config.onlyProfile.literal())
                }

                if (config.exceptProfile.isNotBlank()) {
                    kotlinAnnotation(Quarkus.UnlessBuildProfileAnnotation, config.exceptProfile.literal())
                }

                addPathAnnotation(restController.baseRestPath)

                kotlinMember("delegate", restController.delegate.name.asTypeReference())
                kotlinMember("dependencyVogel", restController.dependencyVogel.name.asTypeReference())

                // produces:
                //
                // private val log = LoggerFactory.getLogger(FeaturesDefaultValueServer::class.java)
                val loggerExpression = Misc.LoggerFactory.identifier()
                    .invoke("getLogger", name.identifier().functionReference("class.java"))
                kotlinMember(
                    "log", Misc.Logger.asTypeReference(), default = loggerExpression, initializedInConstructor = false
                )

                restController.methods.forEach {
                    emitRequest(it)
                }
            }
        }
    }

    context(EmitterContext)
    private fun KotlinClass.emitRequest(method: ServerRestControllerMethod) {
        val restMethod = method.restMethod
        val restPath = method.restPath

        kotlinMethod(method.name, true, Misc.RestResponse.asTypeReference(Kotlin.Star.asTypeReference())) {
            addRequestMethodAnnotation(restMethod)
            addPathAnnotation(restPath)

            // produces:
            //
            // MDC.put("request-method", "<method>")
            // MDC.put("request-path", "<path>")
            // return withContext(MDCContext()) {
            //    ...
            // }
            Misc.MDC.identifier()
                .invoke("put", "request-method".literal(), restMethod.value.literal())
                .statement()
            Misc.MDC.identifier()
                .invoke("put", "request-path".literal(), restPath.literal())
                .statement()
            invoke("withContext".identifier("kotlinx.coroutines"), invoke(Misc.MDCContext.identifier())) {
                // produces:
                //
                // log.info("[<method>] <path> processing request")
                "log".identifier()
                    .invoke("info", "[${restMethod.value}] $restPath - processing request.".literal())
                    .statement()

                val context = object : ServerRestControllerHandlerContext {
                    override fun addParameter(parameter: KotlinParameter) = this@kotlinMethod.addParameter(parameter)
                    override fun addStatement(statement: KotlinStatement) = this@kotlinMethod.addStatement(statement)
                }

                val objectParts = mutableListOf<InstantiationParameter>()
                method.parameters.forEach { parameter ->
                    objectParts += getHandler<ServerRestControllerHandler, InstantiationParameter> {
                        context.emitParameter(parameter)
                    }
                }

                method.body?.let { body ->
                    addConsumesAnnotation(body.content.rawContentType)
                    objectParts += getHandler<ServerRestControllerHandler, InstantiationParameter> {
                        context.emitBody(body)
                    }
                }

                kotlinParameter("headers", Jakarta.HttpHeaders.asTypeReference()) {
                    kotlinAnnotation(Jakarta.ContextAnnotation)
                }

                val container = method.delegateMethod.receiver.container
                if (container != null) {
                    allToObject("request".literal(), container.name.asTypeName(), objectParts)
                        .declaration("request")
                }

                emitDelegateInvocation(method)
            }.returnStatement()
        }
    }

    // generates the call to the delegate
    private fun StatementAware.emitDelegateInvocation(method: ServerRestControllerMethod) {
        val restMethod = method.restMethod
        val restPath = method.restPath

        val context = method.delegateMethod.receiver

        val arguments: MutableList<KotlinExpression> = when (context.container) {
            null -> mutableListOf()
            else -> mutableListOf("request".identifier())
        }

        arguments += "headers".identifier()
        arguments += "dependencyVogel".identifier()

        val requestContextName = invoke(context.name.identifier(), *arguments.toTypedArray())
            .declaration("context")

        tryExpression {
            "delegate".identifier().invoke("run") {
                requestContextName.identifier().invoke(method.delegateMethod.name).statement()
            }.statement()

            val signalName = "requestHandledSignal"
            catchBlock(Library.RequestHandledSignal, signalName) {
                // produces:
                //
                // log.info("[<method>] <path> responding with status code ${requestHandledSignal.response.status}")
                "log".identifier()
                    .invoke(
                        "info",
                        "[${restMethod.value}] $restPath - responding with status code \${requestHandledSignal.response.status}.".literal()
                    )
                    .statement()
                signalName.identifier().property("response").statement()
            }
            catchBlock(Kotlin.Exception, "e") {
                // produces:
                //
                // log.error("[<method>] <path> unexpected exception occurred. Please check the delegate implementation to make sure it never throws an exception. Response is now undefined.")
                // throw e
                "log".identifier()
                    .invoke(
                        "error",
                        "[${restMethod.value}] $restPath - unexpected exception occurred. Please check the delegate implementation to make sure it never throws an exception. Response is now undefined.".literal()
                    )
                    .statement()
                "e".identifier().throwStatement()
            }
        }.statement()
    }

}

interface ServerRestControllerHandlerContext : ParameterAware, StatementAware {

    /**
     * Generates the standard property for a request parameter or request body in the server rest controller.
     */
    fun emitProperty(name: String, type: KotlinTypeReference, annotation: KotlinAnnotation? = null) =
        kotlinParameter(name, type) { annotation?.let { addAnnotation(it) } }

}

interface ServerRestControllerHandler : Handler {

    /**
     * Generates the standard property for a request parameter in the server rest controller.
     */
    fun ServerRestControllerHandlerContext.emitParameter(parameter: RequestParameter): HandlerResult<InstantiationParameter>

    /**
     * Generates the standard property for a request body in the server rest controller.
     */
    fun ServerRestControllerHandlerContext.emitBody(body: RequestBody): HandlerResult<InstantiationParameter>

}
