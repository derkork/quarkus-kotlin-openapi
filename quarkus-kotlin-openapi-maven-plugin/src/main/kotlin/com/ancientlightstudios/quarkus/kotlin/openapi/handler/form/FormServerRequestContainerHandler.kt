package com.ancientlightstudios.quarkus.kotlin.openapi.handler.form

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.ServerRequestContainerEmitter.Companion.emitDefaultRequestContainerBody
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.ServerRequestContainerEmitter.Companion.emitDefaultRequestContainerParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.ServerRequestContainerHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.matches
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinClass
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ServerRequestContainerBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ServerRequestContainerParameter

class FormServerRequestContainerHandler : ServerRequestContainerHandler {

    override fun KotlinClass.emitRequestContainerParameter(
        parameter: ServerRequestContainerParameter, contentType: ContentType
    ) = contentType.matches(ContentType.ApplicationFormUrlencoded) {
        emitDefaultRequestContainerParameter(parameter.name, parameter.content.model)
    }

    override fun KotlinClass.emitRequestContainerBody(
        body: ServerRequestContainerBody, contentType: ContentType
    ) = contentType.matches(ContentType.ApplicationFormUrlencoded) {
        emitDefaultRequestContainerBody(body.name, body.content.model)
    }

}