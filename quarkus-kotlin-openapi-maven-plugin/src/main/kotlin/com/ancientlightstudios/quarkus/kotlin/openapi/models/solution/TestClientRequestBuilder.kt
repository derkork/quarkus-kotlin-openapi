package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiRequest

class TestClientRequestBuilder(
    name: ComponentName,
    val dependencyContainer: DependencyContainer,
    val source: OpenApiRequest
) : SolutionFile(name) {

    val parameters = mutableListOf<RequestParameter>()
    var body: RequestBody? = null

}