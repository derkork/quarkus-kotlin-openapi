package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Feature

class DependencyVogel(name: ComponentName) : SolutionFile(name) {

    val features = mutableSetOf<Feature>()

}