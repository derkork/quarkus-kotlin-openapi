package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiResponse

class ServerResponseInterface(
    name: ComponentName,
    val methodName: String,
    val source: OpenApiResponse
) : SolutionFile(name) {

    val responseCode = source.responseCode
    val headers = mutableListOf<ResponseHeader>()
    var body: ResponseBody? = null

}

