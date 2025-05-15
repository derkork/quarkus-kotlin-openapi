package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiRequest
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiRequestBundle

class ClientRestController(
    name: ComponentName,
    val delegate: ClientDelegateInterface,
    val dependencyContainer: DependencyContainer,
    val source: OpenApiRequestBundle
) : SolutionFile(name) {

    val methods = mutableListOf<ClientRestControllerMethod>()

}

class ClientRestControllerMethod(
    val name: String,
    val delegateMethod: ClientDelegateInterfaceMethod,
    val response: ClientResponse,
    val source: OpenApiRequest
) {

    val parameters = mutableListOf<RequestParameter>()
    var body: RequestBody? = null

}
