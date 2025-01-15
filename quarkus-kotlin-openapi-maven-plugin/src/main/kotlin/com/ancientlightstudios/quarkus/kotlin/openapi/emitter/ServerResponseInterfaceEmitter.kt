package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Handler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerResult
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ResponseBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ResponseHeader
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ServerResponseInterface

class ServerResponseInterfaceEmitter : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.solution.files
            .filterIsInstance<ServerResponseInterface>()
            .forEach { emitFile(it) }
    }

    private fun EmitterContext.emitFile(responseInterface: ServerResponseInterface) {
        kotlinFile(responseInterface.name.asTypeName()) {
            registerImports(Library.All)
            registerImports(config.additionalImports())

            kotlinInterface(name) {
                kotlinMethod(responseInterface.methodName, returnType = Kotlin.Nothing.asTypeReference()) {
                    if (responseInterface.responseCode == ResponseCode.Default) {
                        kotlinParameter("status", Kotlin.Int.asTypeReference())
                    }

                    val context = object : ServerResponseInterfaceHandlerContext {
                        override fun addParameter(parameter: KotlinParameter) =
                            this@kotlinMethod.addParameter(parameter)
                    }

                    responseInterface.body?.let { body ->
                        getHandler<ServerResponseInterfaceHandler, Unit> { context.emitBody(body) }
                    }

                    responseInterface.headers.forEach { header ->
                        getHandler<ServerResponseInterfaceHandler, Unit> { context.emitHeader(header) }
                    }
                }
            }
        }
    }

}

interface ServerResponseInterfaceHandlerContext : ParameterAware {

    /**
     * Generates the standard property for a response header or response body in a server response interface.
     * The type should reflect the nullability of the model even if the value will never be nullable after
     * serialization. Allows maximum flexibility for the code which is providing values.
     */
    fun emitProperty(name: String, type: KotlinTypeReference, defaultValue: DefaultValue) =
        kotlinParameter(name, type, defaultValue.toKotlinExpression())

}

interface ServerResponseInterfaceHandler : Handler {

    /**
     * Generates the standard property for a response header in a server response interface. The type of the property
     * should reflect the nullability of the model even if the value will never be nullable after serialization. Allows
     * maximum flexibility for the code which is providing values.
     */
    fun ServerResponseInterfaceHandlerContext.emitHeader(header: ResponseHeader): HandlerResult<Unit>

    /**
     * Generates the standard property for a response body in a server response interface. The type of the property
     * should reflect the nullability of the model even if the value will never be nullable after serialization. Allows
     * maximum flexibility for the code which is providing values.
     */
    fun ServerResponseInterfaceHandlerContext.emitBody(body: ResponseBody): HandlerResult<Unit>

}
