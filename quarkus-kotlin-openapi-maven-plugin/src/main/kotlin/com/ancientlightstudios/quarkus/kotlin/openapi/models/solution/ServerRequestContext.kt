package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiRequest
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiResponse

class ServerRequestContext(
    name: ComponentName,
    val container: ServerRequestContainer?,
    val dependencyVogel: DependencyVogel,
    val source: OpenApiRequest
) : SolutionFile(name) {

    val restPath = source.path
    val restMethod = source.method
    val methods = mutableListOf<ServerRequestContextResponseMethod>()

}

class ServerRequestContextResponseMethod(
    val name: String,
    val responseInterface: ServerResponseInterface?,
    val source: OpenApiResponse
) {

    val responseCode = source.responseCode
    val headers = mutableListOf<ResponseHeader>()
    var body: ResponseBody? = null

}
