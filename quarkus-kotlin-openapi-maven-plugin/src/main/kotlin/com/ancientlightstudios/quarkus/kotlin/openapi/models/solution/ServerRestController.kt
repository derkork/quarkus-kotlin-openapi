package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiRequest
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiRequestBundle
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.RequestMethod

class ServerRestController(
    name: ComponentName,
    val path: String,
    val delegate: ServerDelegateInterface,
    val dependencyVogel: DependencyVogel,
    val source: OpenApiRequestBundle
) : SolutionFile(name) {

    val methods = mutableListOf<ServerRestControllerMethod>()

}

class ServerRestControllerMethod(
    val name: String,
    val restPath: String,
    val restMethod: RequestMethod,
    val delegateMethod: ServerDelegateInterfaceMethod,
    val source: OpenApiRequest
)
