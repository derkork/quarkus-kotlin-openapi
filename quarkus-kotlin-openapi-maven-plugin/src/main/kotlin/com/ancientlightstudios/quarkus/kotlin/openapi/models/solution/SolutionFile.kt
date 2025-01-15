package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

open class SolutionFile(val name: ComponentName)

open class CompoundSolutionFile(mainComponent: ComponentName, vararg val additionalComponents: ComponentName) :
    SolutionFile(mainComponent)