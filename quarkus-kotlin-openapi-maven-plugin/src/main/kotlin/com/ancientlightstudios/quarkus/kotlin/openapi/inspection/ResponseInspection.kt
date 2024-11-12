package com.ancientlightstudios.quarkus.kotlin.openapi.inspection

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiResponse

class ResponseInspection(val response: OpenApiResponse) {

    fun headers(block: ResponseHeaderInspection.() -> Unit) =
        response.headers.forEach { ResponseHeaderInspection(it).block() }

    fun body(block: BodyInspection.() -> Unit) = response.body?.let { BodyInspection(it).block() }

}