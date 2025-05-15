package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components

import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue

class EnumValidationComponent(val values: List<String>) : SchemaComponent, StructuralComponent {

    override fun merge(other: List<SchemaComponent>, origin: String): Pair<SchemaComponent, List<SchemaComponent>> {
        val (otherMergeComponents, remainingComponents) = other.partitionIsInstance<EnumValidationComponent>()

        // only keep enum values which are defined in all components
        val result = values.toMutableSet()
        otherMergeComponents.forEach { result.retainAll(it.values.toSet()) }

        if (result.isEmpty()) {
            SpecIssue("incompatible enum values detected. Found in schema $origin")
        }

        return EnumValidationComponent(result.toList()) to remainingComponents
    }

}
