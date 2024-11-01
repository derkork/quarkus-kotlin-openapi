package com.ancientlightstudios.quarkus.kotlin.openapi.inspection

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableRequest

class RequestInspection(val request: TransformableRequest) {

    fun parameters(block: ParameterInspection.() -> Unit) =
        request.parameters.forEach { ParameterInspection(it).block() }

    fun body(block: BodyInspection.() -> Unit) = request.body?.let { BodyInspection(it).block() }

    fun responses(block: ResponseInspection.() -> Unit) = request.responses.forEach { ResponseInspection(it).block() }

}