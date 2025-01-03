package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components

import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue

class DefaultComponent(val default: String) : SchemaComponent, MetaComponent {

    override fun merge(other: List<SchemaComponent>, origin: String): Pair<SchemaComponent, List<SchemaComponent>> {
        val (otherMergeComponents, remainingComponents) = other.partitionIsInstance<DefaultComponent>()

        val hasIncompatibleValues = otherMergeComponents.any { it.default != default }
        if (hasIncompatibleValues) {
            SpecIssue("different default values detected. Found in schema $origin")
        }

        return this to remainingComponents
    }

}