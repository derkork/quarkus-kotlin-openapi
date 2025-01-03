package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiRequest
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiResponse
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.RequestMethod
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseCode

class ServerRequestContext(
    name: ComponentName,
    val restPath: String,
    val restMethod: RequestMethod,
    val container: ServerRequestContainer?,
    val source: OpenApiRequest
) : SolutionFile(name) {

    val methods = mutableListOf<ServerRequestContextResponseMethod>()

}

class ServerRequestContextResponseMethod(
    val name: String,
    val responseCode: ResponseCode,
    val responseInterface: ServerResponseInterface?,
    val source: OpenApiResponse
)
