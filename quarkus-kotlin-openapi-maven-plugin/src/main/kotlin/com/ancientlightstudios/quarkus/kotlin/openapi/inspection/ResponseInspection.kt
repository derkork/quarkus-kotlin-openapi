package com.ancientlightstudios.quarkus.kotlin.openapi.inspection

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableResponse

class ResponseInspection(val response: TransformableResponse) {

    // TODO: headers

    fun body(block: BodyInspection.() -> Unit) = response.body?.let { BodyInspection(it).block() }

}