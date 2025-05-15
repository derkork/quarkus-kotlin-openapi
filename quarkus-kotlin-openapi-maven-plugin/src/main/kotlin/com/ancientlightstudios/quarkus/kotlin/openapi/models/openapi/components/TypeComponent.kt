package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.SchemaType
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue

class TypeComponent(val type: SchemaType) : SchemaComponent, StructuralComponent {

    override fun merge(other: List<SchemaComponent>, origin: String): Pair<SchemaComponent, List<SchemaComponent>> {
        val (otherMergeComponents, remainingComponents) = other.partitionIsInstance<TypeComponent>()

        val hasIncompatibleValues = otherMergeComponents.any { it.type != type }
        if (hasIncompatibleValues) {
            SpecIssue("different types detected. Found in schema $origin")
        }

        return this to remainingComponents
    }


}