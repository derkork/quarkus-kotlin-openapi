package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiRequest

class ServerRequestContainer(name: ComponentName, val source: OpenApiRequest) : SolutionFile(name) {

    val parameters = mutableListOf<ServerRequestContainerParameter>()
    var body: ServerRequestContainerBody? = null

}

class ServerRequestContainerParameter(val name: String, val content: ContentInfo, val source: OpenApiParameter)

class ServerRequestContainerBody(val name: String, val content: ContentInfo, val source: OpenApiBody)