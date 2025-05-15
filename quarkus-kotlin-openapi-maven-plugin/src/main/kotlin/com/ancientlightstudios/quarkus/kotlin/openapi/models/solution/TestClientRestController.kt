package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiRequest
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiRequestBundle

class TestClientRestController(
    name: ComponentName,
    val dependencyContainer: DependencyContainer,
    val source: OpenApiRequestBundle
) : SolutionFile(name) {

    val methods = mutableListOf<TestClientRestControllerMethod>()

}

class TestClientRestControllerMethod(
    val name: String,
    val builder: TestClientRequestBuilder,
    val response: ClientResponse,
    val validator: TestClientResponseValidator,
    val source: OpenApiRequest
) {

    val restPath = source.path
    val restMethod = source.method
    val parameters = mutableListOf<RequestParameter>()
    var body: RequestBody? = null

}
