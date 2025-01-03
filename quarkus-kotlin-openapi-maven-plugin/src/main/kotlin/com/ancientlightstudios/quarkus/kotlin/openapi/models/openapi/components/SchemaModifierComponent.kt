package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.SchemaModifier
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue

class SchemaModifierComponent(val modifier: SchemaModifier) : SchemaComponent, MetaComponent {

    override fun merge(other: List<SchemaComponent>, origin: String): Pair<SchemaComponent, List<SchemaComponent>> {
        val (otherMergeComponents, remainingComponents) = other.partitionIsInstance<SchemaModifierComponent>()

        val hasIncompatibleValues = otherMergeComponents.any { it.modifier != modifier }
        if (hasIncompatibleValues) {
            SpecIssue("different schema modifier values detected. Found in schema $origin")
        }

        return this to remainingComponents
    }
    
}