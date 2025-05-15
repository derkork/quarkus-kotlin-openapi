package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components

import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue

class FormatComponent(val format: String) : SchemaComponent, StructuralComponent {

    override fun merge(other: List<SchemaComponent>, origin: String): Pair<SchemaComponent, List<SchemaComponent>> {
        val (otherMergeComponents, remainingComponents) = other.partitionIsInstance<FormatComponent>()

        val hasIncompatibleItems = otherMergeComponents.any { it.format != format }
        if (hasIncompatibleItems) {
            SpecIssue("different format values detected. Found in schema $origin")
        }

        return this to remainingComponents
    }

}