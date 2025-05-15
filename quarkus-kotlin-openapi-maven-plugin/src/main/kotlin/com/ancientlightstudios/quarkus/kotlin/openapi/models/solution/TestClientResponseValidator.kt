package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiRequest

class TestClientResponseValidator(
    name: ComponentName,
    val response: ClientResponse,
    val source: OpenApiRequest,
) : SolutionFile(name)