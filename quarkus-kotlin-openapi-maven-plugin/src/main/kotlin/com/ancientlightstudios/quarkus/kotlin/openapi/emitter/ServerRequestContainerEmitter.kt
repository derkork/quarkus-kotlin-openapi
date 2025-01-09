package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.ContentTypeHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinClass
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.Library
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinClass
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinMember
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
            registerImports(Library.All)

            kotlinClass(name) {
                container.parameters.forEach { parameter ->
                    getHandler<ServerRequestContainerHandler>(parameter.content.contentType).run {
                        emitRequestContainerParameter(parameter)
                    }
                }

                container.body?.let { body ->
                    getHandler<ServerRequestContainerHandler>(body.content.contentType).run {
                        emitRequestContainerBody(body)
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

interface ServerRequestContainerHandler : ContentTypeHandler {

    fun KotlinClass.emitRequestContainerParameter(parameter: ServerRequestContainerParameter)

    fun KotlinClass.emitRequestContainerBody(body: ServerRequestContainerBody)

}
