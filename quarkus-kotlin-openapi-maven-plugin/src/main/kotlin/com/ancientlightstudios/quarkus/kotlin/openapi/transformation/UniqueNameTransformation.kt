package com.ancientlightstudios.quarkus.kotlin.openapi.transformation

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.CompoundSolutionFile
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ConflictResolution

class UniqueNameTransformation : SpecTransformation {

    override fun TransformationContext.perform() {
        val nameLookup = mutableMapOf<String, Int>()

        val allNames = spec.solution.files.flatMap {
            when (it) {
                is CompoundSolutionFile -> listOf(it.name) + it.additionalComponents
                else -> listOf(it.name)
            }
        }
            .groupBy { it.strategy }

        val pinnedNames = allNames[ConflictResolution.Pinned] ?: listOf()
        val requestedNames = allNames[ConflictResolution.Requested] ?: listOf()
        val generatedNames = allNames[ConflictResolution.Generated] ?: listOf()

        val prioritizedNames = pinnedNames + requestedNames + generatedNames
        prioritizedNames.forEach {
            // the returned counter is either 0 if this is the first time this name is processed, or increased by one
            // as the computation never returns a null value the null check at the end is not necessary, but makes the
            // compiler happy :)
            val counter = nameLookup.compute(it.name) { _, amount -> amount?.plus(1) ?: 0 } ?: 0

            // only if the name was processed before, we have to modify it
            if (counter > 0) {
                it.name = "${it.name}$counter"
            }
        }
    }

}
