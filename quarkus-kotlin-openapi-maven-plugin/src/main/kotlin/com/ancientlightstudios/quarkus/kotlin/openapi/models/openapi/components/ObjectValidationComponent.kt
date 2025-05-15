package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components

class ObjectValidationComponent(val required: List<String> = listOf()) : SchemaComponent, StructuralComponent {

    override fun merge(other: List<SchemaComponent>, origin: String): Pair<SchemaComponent, List<SchemaComponent>> {
        val (otherMergeComponents, remainingComponents) = other.partitionIsInstance<ObjectValidationComponent>()

        // just append everything into a single list
        val result = required.toMutableList()
        otherMergeComponents.flatMapTo(result) { it.required }
        return ObjectValidationComponent(result) to remainingComponents
    }

}