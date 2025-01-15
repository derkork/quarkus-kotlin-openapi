package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.*

class ServerRestController(
    name: ComponentName,
    val baseRestPath: String,
    val delegate: ServerDelegateInterface,
    val dependencyVogel: DependencyVogel,
    val source: OpenApiRequestBundle
) : SolutionFile(name) {

    val methods = mutableListOf<ServerRestControllerMethod>()

}

class ServerRestControllerMethod(
    val name: String,
    val delegateMethod: ServerDelegateInterfaceMethod,
    val source: OpenApiRequest
) {

    val restPath = source.path
    val restMethod = source.method
    val parameters = mutableListOf<RequestParameter>()
    var body: RequestBody? = null

}
