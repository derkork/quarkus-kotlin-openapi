package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.Solution

// contains the solution for the api
object SolutionHint : Hint<Solution> {

    val OpenApiSpec.solution: Solution
        get() = getOrPut(SolutionHint) { Solution() }

}