package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Handler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerResult
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.RequestBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.RequestParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ServerRequestContainer

class ServerRequestContainerEmitter : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.solution.files
            .filterIsInstance<ServerRequestContainer>()
            .forEach { emitFile(it) }
    }

    private fun EmitterContext.emitFile(container: ServerRequestContainer) {
        kotlinFile(container.name.asTypeName()) {
            kotlinClass(name) {
                val context = object : ServerRequestContainerHandlerContext {
                    override fun addMember(member: KotlinMember) = this@kotlinClass.addMember(member)
                }

                container.parameters.forEach { parameter ->
                    getHandler<ServerRequestContainerHandler, Unit> { context.emitParameter(parameter) }
                }

                container.body?.let { body ->
                    getHandler<ServerRequestContainerHandler, Unit> { context.emitBody(body) }
                }
            }
        }
    }

}

interface ServerRequestContainerHandlerContext : MemberAware {

    /**
     * Generates the standard property for a request parameter or request body in the server request container.
     * The nullability of the type should reflect the modifications done to the value by the deserialization.
     */
    fun emitProperty(name: String, type: KotlinTypeReference) = kotlinMember(name, type, accessModifier = null)

}


interface ServerRequestContainerHandler : Handler {

    /**
     * Generates the standard property for a request parameter in the server request container. The nullability of
     * the type of the property should reflect the modifications done to the value by the deserialization.
     */
    fun ServerRequestContainerHandlerContext.emitParameter(parameter: RequestParameter): HandlerResult<Unit>

    /**
     * Generates the standard property for a request body in the server request container. The nullability of
     * the type of the property should reflect the modifications done to the value by the deserialization.
     */
    fun ServerRequestContainerHandlerContext.emitBody(body: RequestBody): HandlerResult<Unit>

}
