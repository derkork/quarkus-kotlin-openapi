package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiRequest
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiResponse

class ClientResponse(
    name: ComponentName,
    val httpResponse: ClientHttpResponse,
    val errorResponse: ClientErrorResponse,
    val source: OpenApiRequest
) : CompoundSolutionFile(name, httpResponse.name, errorResponse.name)

class ClientHttpResponse(val name: ComponentName) {

    val implementations = mutableListOf<ClientResponseImplementation>()

}

class ClientErrorResponse(val name: ComponentName)

class ClientResponseImplementation(val name: String, val source: OpenApiResponse) {

    val responseCode = source.responseCode
    val headers = mutableListOf<ResponseHeader>()
    var body: ResponseBody? = null

}
