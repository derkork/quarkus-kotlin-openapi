package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiRequest

class ServerRequestContainer(name: ComponentName, val source: OpenApiRequest) : SolutionFile(name)