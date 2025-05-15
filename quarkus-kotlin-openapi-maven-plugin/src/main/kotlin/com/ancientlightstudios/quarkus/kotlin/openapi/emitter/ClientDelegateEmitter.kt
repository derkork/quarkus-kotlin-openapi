package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Handler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerResult
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ClientDelegateInterface
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ClientDelegateInterfaceMethod
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.RequestBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.RequestParameter

class ClientDelegateEmitter : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.solution.files
            .filterIsInstance<ClientDelegateInterface>()
            .forEach { emitFile(it) }
    }

    private fun EmitterContext.emitFile(delegateInterface: ClientDelegateInterface) {
        kotlinFile(delegateInterface.name.asTypeName()) {
            registerImports(Library.All)
            registerImports(config.additionalImports())

            kotlinInterface(name) {
                kotlinAnnotation(Misc.RegisterRestClient, "configKey" to delegateInterface.clientName.literal())
                addPathAnnotation(delegateInterface.baseRestPath)

                config.additionalProviders().forEach {
                    kotlinAnnotation(Misc.RegisterProvider, it.identifier().functionReference("class"))
                }

                delegateInterface.methods.forEach {
                    emitRequest(it)
                }
            }
        }
    }

    context(EmitterContext)
    private fun KotlinInterface.emitRequest(method: ClientDelegateInterfaceMethod) {
        val restMethod = method.restMethod
        val restPath = method.restPath

        kotlinMethod(
            method.name,
            true,
            Misc.RestResponse.asTypeReference(Kotlin.ByteArray.asTypeReference().acceptNull())
        ) {
            addRequestMethodAnnotation(restMethod)
            addPathAnnotation(restPath)

            val context = object : ClientDelegateHandlerContext {
                override fun addParameter(parameter: KotlinParameter) = this@kotlinMethod.addParameter(parameter)
            }

            method.parameters.forEach { parameter ->
                getHandler<ClientDelegateHandler, Unit> { context.emitParameter(parameter) }
            }

            method.body?.let { body ->
                addConsumesAnnotation(body.content.rawContentType)
                getHandler<ClientDelegateHandler, Unit> { context.emitBody(body) }
            }
        }
    }

}

interface ClientDelegateHandlerContext : ParameterAware {

    /**
     * Generates the standard property for a request parameter or request body in the client delegate.
     * The nullability of the type should reflect the modifications done to the value by the serialization.
     */
    fun emitProperty(name: String, type: KotlinTypeReference, annotation: KotlinAnnotation? = null) =
        kotlinParameter(name, type) { annotation?.let { addAnnotation(it) } }

}

interface ClientDelegateHandler : Handler {

    /**
     * Emits the property for a request parameter in the client delegate. The nullability of the type of the property
     * should reflect the modifications done to the value by the serialization.
     */
    fun ClientDelegateHandlerContext.emitParameter(parameter: RequestParameter): HandlerResult<Unit>

    /**
     * Emits the property for a request body in the client delegate. The nullability of the type of the property
     * should reflect the modifications done to the value by the serialization.
     */
    fun ClientDelegateHandlerContext.emitBody(body: RequestBody): HandlerResult<Unit>

}
