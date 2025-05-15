package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components

import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class OneOfComponent(override val options: List<SomeOfOption>, val discriminator: OneOfDiscriminator?) :
    SomeOfComponent {

    override fun merge(other: List<SchemaComponent>, origin: String): Pair<SchemaComponent, List<SchemaComponent>> {
        val (otherMergeComponents, remainingComponents) = other.partitionIsInstance<OneOfComponent>()

        if (otherMergeComponents.isNotEmpty()) {
            ProbableBug("merging oneOf components not yet implemented. Please submit an example of your use-case, so we can support this.. Found in schema $origin")
        }

        return this to remainingComponents
    }

}

class OneOfDiscriminator(val property: String, val additionalMappings: Map<String, String>)
