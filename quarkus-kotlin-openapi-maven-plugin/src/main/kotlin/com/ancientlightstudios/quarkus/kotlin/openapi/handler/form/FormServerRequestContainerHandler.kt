package com.ancientlightstudios.quarkus.kotlin.openapi.handler.form

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.ServerRequestContainerEmitter.Companion.emitDefaultRequestContainerBody
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.ServerRequestContainerEmitter.Companion.emitDefaultRequestContainerParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.ServerRequestContainerHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinClass
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.DependencyVogel
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ServerRequestContainerBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ServerRequestContainerParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.DependencyVogelHandler

class FormServerRequestContainerHandler : ServerRequestContainerHandler, DependencyVogelHandler {

    override val supportedContentType = ContentType.ApplicationFormUrlencoded

    override fun KotlinClass.emitRequestContainerParameter(parameter: ServerRequestContainerParameter) {
        emitDefaultRequestContainerParameter(parameter.name, parameter.content.model)
    }

    override fun KotlinClass.emitRequestContainerBody(body: ServerRequestContainerBody) {
        emitDefaultRequestContainerBody(body.name, body.content.model)
    }

    override fun installFeatureFor(dependencyVogel: DependencyVogel) {
        // TODO: based on the type of the property models or the content encoding, ask other handler to install features
        // e.g. json
    }

}