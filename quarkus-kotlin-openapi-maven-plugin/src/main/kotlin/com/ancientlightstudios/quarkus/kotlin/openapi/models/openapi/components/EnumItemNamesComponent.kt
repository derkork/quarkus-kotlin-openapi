package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components

class EnumItemNamesComponent(val values: Map<String, String>) : SchemaComponent, StructuralComponent {

    override fun merge(other: List<SchemaComponent>, origin: String): Pair<SchemaComponent, List<SchemaComponent>> {
        val (otherMergeComponents, remainingComponents) = other.partitionIsInstance<EnumItemNamesComponent>()

        val result = values.toMutableMap()
        // we just merge them together
        otherMergeComponents.forEach { result.putAll(it.values) }
        return EnumItemNamesComponent(result) to remainingComponents
    }

}