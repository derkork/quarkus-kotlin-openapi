package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components

import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class AnyOfComponent(override val options: List<SomeOfOption>) : SomeOfComponent {

    override fun merge(other: List<SchemaComponent>, origin: String): Pair<SchemaComponent, List<SchemaComponent>> {
        val (otherMergeComponents, remainingComponents) = other.partitionIsInstance<AnyOfComponent>()

        if (otherMergeComponents.isNotEmpty()) {
            ProbableBug("merging anyOf components not yet implemented. Please submit an example of your use-case, so we can support this. Found in schema $origin")
        }

        return this to remainingComponents
    }

}
