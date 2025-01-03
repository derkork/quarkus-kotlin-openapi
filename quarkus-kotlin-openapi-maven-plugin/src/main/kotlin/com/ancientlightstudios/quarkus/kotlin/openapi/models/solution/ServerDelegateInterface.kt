package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiRequest
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiRequestBundle

class ServerDelegateInterface(name: ComponentName, val source: OpenApiRequestBundle) : SolutionFile(name) {

    val methods = mutableListOf<ServerDelegateInterfaceMethod>()

}

class ServerDelegateInterfaceMethod(val name: String, val receiver: ServerRequestContext, val source: OpenApiRequest)