package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiRequest
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiRequestBundle

class ClientDelegateInterface(
    name: ComponentName,
    val clientName: String,
    val baseRestPath: String,
    val source: OpenApiRequestBundle
) : SolutionFile(name) {

    val methods = mutableListOf<ClientDelegateInterfaceMethod>()

}

class ClientDelegateInterfaceMethod(val name: String, val source: OpenApiRequest) {

    val restPath = source.path
    val restMethod = source.method
    val parameters = mutableListOf<RequestParameter>()
    var body: RequestBody? = null

}