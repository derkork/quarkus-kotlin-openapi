package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Handler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerResult
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ModelUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ServerResponseBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ServerResponseHeader
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

                    responseInterface.body?.let { body ->
                        getHandler<ServerResponseInterfaceHandler, Unit> {
                            emitResponseInterfaceBody(body, body.content.contentType)
                        }
                    }

                    responseInterface.headers.forEach { header ->
                        getHandler<ServerResponseInterfaceHandler, Unit> {
                            emitResponseInterfaceHeader(header, header.content.contentType)
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun KotlinMethod.emitDefaultResponseInterfaceHeader(
            name: String, model: ModelUsage, defaultValue: DefaultValue
        ) {
            kotlinParameter(name, model.asTypeReference(), defaultValue.toKotlinExpression())
        }

        fun KotlinMethod.emitDefaultResponseInterfaceBody(name: String, model: ModelUsage, defaultValue: DefaultValue) {
            kotlinParameter(name, model.asTypeReference(), defaultValue.toKotlinExpression())
        }

    }
}

interface ServerResponseInterfaceHandler : Handler {

    fun KotlinMethod.emitResponseInterfaceHeader(
        header: ServerResponseHeader, contentType: ContentType
    ): HandlerResult<Unit>

    fun KotlinMethod.emitResponseInterfaceBody(body: ServerResponseBody, contentType: ContentType): HandlerResult<Unit>

}
