package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiResponse
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseCode

// TODO: Headers, Body
class ServerResponseInterface(
    name: ComponentName,
    val methodName: String,
    val responseCode: ResponseCode,
    val source: OpenApiResponse
) : SolutionFile(name)