package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiRequest
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiResponse
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseCode

class ServerRequestContext(name: FileName, val source: OpenApiRequest) : SolutionFile(name) {

    val methods = mutableListOf<ServerRequestContextResponseMethod>()

}

class ServerRequestContextResponseMethod(
    val responseCode: ResponseCode,
    val source: OpenApiResponse
)
