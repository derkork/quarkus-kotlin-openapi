package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchemaProperty
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class ObjectComponent(val properties: List<OpenApiSchemaProperty> = listOf()) : SchemaComponent,
    StructuralComponent {

    override fun merge(other: List<SchemaComponent>, origin: String): Pair<SchemaComponent, List<SchemaComponent>> {
        val (otherMergeComponents, remainingComponents) = other.partitionIsInstance<ObjectComponent>()

        // just append everything into a single list
        val result = properties.toMutableList()
        otherMergeComponents.flatMapTo(result) { it.properties }

        val uniqueNames = result.map { it.name }.toSet()
        if (result.size > uniqueNames.size) {
            ProbableBug("Extending object properties not yet implemented. Please submit an example of your use-case, so we can support this. Found in schema $origin")
        }

        return ObjectComponent(result) to remainingComponents
    }

}