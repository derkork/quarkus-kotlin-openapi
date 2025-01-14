package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Handler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerResult
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinClass
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.Library
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinClass
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinMember
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ModelUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ServerRequestContainer
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ServerRequestContainerBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ServerRequestContainerParameter

class ServerRequestContainerEmitter : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.solution.files
            .filterIsInstance<ServerRequestContainer>()
            .forEach { emitFile(it) }
    }

    private fun EmitterContext.emitFile(container: ServerRequestContainer) {
        kotlinFile(container.name.asTypeName()) {
            kotlinClass(name) {
                container.parameters.forEach { parameter ->
                    getHandler<ServerRequestContainerHandler, Unit> {
                        emitRequestContainerParameter(parameter, parameter.content.contentType)
                    }
                }

                container.body?.let { body ->
                    getHandler<ServerRequestContainerHandler, Unit> {
                        emitRequestContainerBody(body, body.content.contentType)
                    }
                }
            }
        }
    }

    companion object {

        fun KotlinClass.emitDefaultRequestContainerParameter(name: String, model: ModelUsage) =
            kotlinMember(name, model.asTypeReference(), accessModifier = null)

        fun KotlinClass.emitDefaultRequestContainerBody(name: String, model: ModelUsage) =
            kotlinMember(name, model.asTypeReference(), accessModifier = null)

    }
}

/**
 * handler of this type are responsible to generate class members for a request container
 */
interface ServerRequestContainerHandler : Handler {

    fun KotlinClass.emitRequestContainerParameter(
        parameter: ServerRequestContainerParameter, contentType: ContentType
    ): HandlerResult<Unit>

    fun KotlinClass.emitRequestContainerBody(
        body: ServerRequestContainerBody, contentType: ContentType
    ): HandlerResult<Unit>

}
