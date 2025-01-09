package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiResponse
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseCode

class ServerResponseInterface(
    name: ComponentName,
    val methodName: String,
    val responseCode: ResponseCode,
    val source: OpenApiResponse
) : SolutionFile(name) {

    val headers = mutableListOf<ServerResponseHeader>()
    var body: ServerResponseBody? = null

}

class ServerResponseHeader(
    val name: String,
    val sourceName: String,
    val content: ContentInfo,
    val source: OpenApiParameter
)

class ServerResponseBody(val name: String, val content: ContentInfo, val source: OpenApiBody)